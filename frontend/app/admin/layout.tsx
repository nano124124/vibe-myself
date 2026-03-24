export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ display: 'flex' }}>
      <aside>
        <nav>Admin Menu</nav>
      </aside>
      <main>{children}</main>
    </div>
  )
}
