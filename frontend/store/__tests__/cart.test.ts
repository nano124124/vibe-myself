import { describe, it, expect, beforeEach } from 'vitest'
import { useCartStore } from '../cart'

describe('cart store', () => {
  beforeEach(() => {
    useCartStore.setState({ items: [] })
  })

  it('starts with empty items', () => {
    const { items } = useCartStore.getState()
    expect(items).toEqual([])
  })

  it('adds an item to cart', () => {
    const { addItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(1)
    expect(items[0].name).toBe('상품A')
  })

  it('increments quantity if same item added again', () => {
    const { addItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(1)
    expect(items[0].quantity).toBe(2)
  })

  it('removes an item from cart', () => {
    const { addItem, removeItem } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    removeItem(1)
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(0)
  })

  it('clears all items', () => {
    const { addItem, clearCart } = useCartStore.getState()
    addItem({ id: 1, name: '상품A', price: 10000, quantity: 1 })
    addItem({ id: 2, name: '상품B', price: 20000, quantity: 2 })
    clearCart()
    const { items } = useCartStore.getState()
    expect(items).toHaveLength(0)
  })
})
