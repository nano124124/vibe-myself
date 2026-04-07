import type { ReactNode } from 'react'
import { Toaster } from 'sonner'
import '@fontsource/pretendard/400.css'
import '@fontsource/pretendard/500.css'
import '@fontsource/pretendard/600.css'
import '@fontsource/pretendard/700.css'

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div style={{ fontFamily: "'Pretendard', sans-serif" }}>
      {children}
      <Toaster position="top-right" richColors />
    </div>
  )
}
