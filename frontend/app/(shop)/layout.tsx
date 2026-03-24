export default function ShopLayout({ children }: { children: React.ReactNode }) {
  return (
    <div>
      <header>
        <nav>Vibe Myself</nav>
      </header>
      <main>{children}</main>
      <footer>Footer</footer>
    </div>
  )
}
