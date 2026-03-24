import { describe, it, expect, beforeEach } from 'vitest'

describe('api instance', () => {
  beforeEach(() => {
    process.env.NEXT_PUBLIC_API_URL = 'http://localhost:8080'
  })

  it('exports an axios instance', async () => {
    const { default: api } = await import('../api')
    expect(api).toBeDefined()
    expect(typeof api.get).toBe('function')
    expect(typeof api.post).toBe('function')
  })

  it('sets withCredentials true for cookie forwarding', async () => {
    const { default: api } = await import('../api')
    expect(api.defaults.withCredentials).toBe(true)
  })

  it('sets Content-Type to application/json', async () => {
    const { default: api } = await import('../api')
    expect(api.defaults.headers.common['Content-Type']).toBe('application/json')
  })
})
