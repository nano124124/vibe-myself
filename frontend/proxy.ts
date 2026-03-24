import { NextRequest, NextResponse } from 'next/server'
import { getRedirectPath } from '@/lib/authGuard'

export function proxy(request: NextRequest) {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = getRedirectPath(token)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/admin/:path*'],
}
