package com.example.lipsticks.tryon.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.tryon.dto.LipPoint;
import com.example.lipsticks.tryon.dto.TryOnResult;
import com.example.lipsticks.tryon.entity.TryOnRecord;
import com.example.lipsticks.tryon.mapper.TryOnRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TryOnService {

    private final LipstickProductMapper lipstickProductMapper;
    private final TryOnRecordMapper tryOnRecordMapper;

    public TryOnResult upload(Long productId, MultipartFile file, String username,
                              List<LipPoint> outerLip, List<LipPoint> innerLip) {
        LipstickProduct product = lipstickProductMapper.selectById(productId);
        if (product == null || Boolean.FALSE.equals(product.getOnSale())) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }

        String originalFilename = file.getOriginalFilename() == null ? "unknown.jpg" : file.getOriginalFilename();

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("无法读取图片文件，请上传有效的 JPG/PNG 图片");
            }

            String colorHex = product.getColorHex() != null ? product.getColorHex() : "#B3123F";
            BufferedImage resultImage = applyLipstick(originalImage, outerLip, innerLip, colorHex);

            String originalBase64 = imageToBase64(originalImage);
            String resultBase64 = imageToBase64(resultImage);

            TryOnRecord record = new TryOnRecord();
            record.setUsername(username);
            record.setProductId(productId);
            record.setOriginalFilename(originalFilename);
            record.setResultFilename("tryon_" + System.currentTimeMillis() + "_" + productId + ".png");
            record.setCreatedAt(LocalDateTime.now());
            tryOnRecordMapper.insert(record);

            log.info("TryOn request: user={}, product={}, file={}, recordId={}",
                    username, product.getTitle(), originalFilename, record.getId());

            return new TryOnResult(
                    product.getId(),
                    product.getTitle(),
                    colorHex,
                    originalBase64,
                    resultBase64,
                    "试妆完成"
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("TryOn processing failed: user={}, product={}", username, product.getTitle(), e);
            throw new RuntimeException("图片处理失败，请重试", e);
        }
    }

    private BufferedImage applyLipstick(BufferedImage original, List<LipPoint> outerLip,
                                        List<LipPoint> innerLip, String colorHex) {
        BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(original, 0, 0, null);

        Color lipstickColor = parseHexColor(colorHex);

        BufferedImage lipMask = createLipMask(original.getWidth(), original.getHeight(), outerLip, innerLip);

        BufferedImage colorLayer = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D colorG = colorLayer.createGraphics();
        colorG.setColor(lipstickColor);
        colorG.fillRect(0, 0, original.getWidth(), original.getHeight());
        colorG.dispose();

        BufferedImage maskedColor = applyMask(colorLayer, lipMask, 0.55f);

        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.drawImage(maskedColor, 0, 0, null);

        BufferedImage blurMask = expandMask(lipMask, 4);
        BufferedImage blurColor = applyMask(colorLayer, blurMask, 0.25f);
        g2d.drawImage(blurColor, 0, 0, null);

        g2d.dispose();
        return result;
    }

    private BufferedImage createLipMask(int width, int height, List<LipPoint> outerLip, List<LipPoint> innerLip) {
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mask.createGraphics();

        g.setColor(Color.WHITE);
        drawSmoothPolygon(g, outerLip);

        g.setComposite(AlphaComposite.Clear);
        g.setColor(Color.WHITE);
        drawSmoothPolygon(g, innerLip);

        g.dispose();
        return mask;
    }

    private void drawSmoothPolygon(Graphics2D g, List<LipPoint> points) {
        if (points.size() < 3) return;

        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xPoints[i] = (int) Math.round(points.get(i).getX());
            yPoints[i] = (int) Math.round(points.get(i).getY());
        }
        g.fillPolygon(xPoints, yPoints, points.size());
    }

    private BufferedImage applyMask(BufferedImage colorImage, BufferedImage mask, float alpha) {
        BufferedImage result = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int w = colorImage.getWidth();
        int h = colorImage.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int maskPixel = mask.getRGB(x, y);
                int maskAlpha = (maskPixel >> 24) & 0xFF;
                if (maskAlpha > 0) {
                    int colorPixel = colorImage.getRGB(x, y);
                    int r = (colorPixel >> 16) & 0xFF;
                    int gv = (colorPixel >> 8) & 0xFF;
                    int b = colorPixel & 0xFF;
                    int a = (int) (maskAlpha * alpha / 255.0f * 255);
                    result.setRGB(x, y, (a << 24) | (r << 16) | (gv << 8) | b);
                }
            }
        }
        return result;
    }

    private BufferedImage expandMask(BufferedImage mask, int expand) {
        int w = mask.getWidth();
        int h = mask.getHeight();
        BufferedImage expanded = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean near = false;
                for (int dy = -expand; dy <= expand && !near; dy++) {
                    for (int dx = -expand; dx <= expand && !near; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int pixel = mask.getRGB(nx, ny);
                            int alpha = (pixel >> 24) & 0xFF;
                            if (alpha > 0) {
                                near = true;
                            }
                        }
                    }
                }
                if (near) {
                    int origAlpha = (mask.getRGB(x, y) >> 24) & 0xFF;
                    expanded.setRGB(x, y, (origAlpha << 24) | 0xFFFFFF);
                }
            }
        }
        return expanded;
    }

    private Color parseHexColor(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(clean.substring(0, 2), 16);
        int g = Integer.parseInt(clean.substring(2, 4), 16);
        int b = Integer.parseInt(clean.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    private String imageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
