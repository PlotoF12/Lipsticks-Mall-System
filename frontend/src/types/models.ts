export interface LipstickProduct {
  id: number;
  title: string;
  brand: string | null;
  shade: string | null;
  colorHex: string | null;
  category: string | null;
  finishType: string | null;
  detail: string | null;
  price: number;
  stock: number;
  onSale: boolean;
  suitableSkinTone: string | null;
  suitableGender: string | null;
  scene: string | null;
  imageUrl: string | null;
}

export interface RecommendItem {
  productId: number;
  title: string;
  brand: string;
  shade: string;
  colorHex: string;
  category: string;
  price: number;
  reason: string;
}

export interface ColorSwatch {
  productId: number;
  title: string;
  brand: string;
  shade: string;
  colorHex: string;
  category: string;
}

export interface TryOnResult {
  productId: number;
  productTitle: string;
  colorHex: string;
  input: string;
  output: string;
  message: string;
}

export interface UserMe {
  username: string;
  role: string;
  gender: string | null;
  skinTone: string | null;
  skinType: string | null;
}

export interface AdminUserRow {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
  gender: string | null;
  skinTone: string | null;
  skinType: string | null;
}
