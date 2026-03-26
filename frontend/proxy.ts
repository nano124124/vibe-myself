import { NextRequest, NextResponse } from 'next/server'
import { getRedirectPath } from '@/lib/authGuard'

export const proxy = (request: NextRequest) => {
  const token = request.cookies.get('access_token')?.value ?? null
  const redirectPath = getRedirectPath(token, request.nextUrl.pathname)

  if (redirectPath) {
    return NextResponse.redirect(new URL(redirectPath, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/admin', '/admin/:path*'],
}
