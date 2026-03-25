import AdminHeader from '@/components/system/AdminHeader'

export default function AdminMainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <AdminHeader />
      <div className="flex flex-1">
        <aside className="w-48 border-r border-slate-200 bg-white p-4">
          <nav className="text-sm text-slate-600">Admin Menu</nav>
        </aside>
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}
