export interface LoginAdminRequest {
  loginId: string
  password: string
}

export interface MenuResponse {
  menuNo: number // Java Long → JS number (현재 메뉴 수 기준 MAX_SAFE_INTEGER 범위 내 안전)
  menuNm: string
  menuUrl: string | null
  sortOrd: number
  children: MenuResponse[]
}
