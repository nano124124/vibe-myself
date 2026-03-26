'use client'

import { Button } from '@/components/ui/button'
import { useAdminLogout } from '@/hooks/system/useAdminLogout'

const AdminHeader = () => {
  const { mutate: logout, isPending } = useAdminLogout()

  return (
    <header className="flex h-14 items-center justify-between border-b border-slate-200 bg-white px-6">
      <span className="text-sm font-semibold text-slate-700">Vibe Admin</span>
      <Button
        variant="outline"
        size="sm"
        onClick={() => logout()}
        disabled={isPending}
      >
        {isPending ? '로그아웃 중...' : '로그아웃'}
      </Button>
    </header>
  )
}

export default AdminHeader
