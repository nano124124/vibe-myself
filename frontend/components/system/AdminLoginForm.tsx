'use client'

import { useState } from 'react'
import { isAxiosError } from 'axios'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAdminLogin } from '@/hooks/system/useAdminLogin'

const AdminLoginForm = () => {
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const mutation = useAdminLogin()

  const errorMessage = (() => {
    if (!mutation.error) return null
    if (isAxiosError(mutation.error) && mutation.error.response?.status === 401) {
      return '아이디 또는 비밀번호가 올바르지 않습니다.'
    }
    return '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  })()

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({ loginId, password })
  }

  return (
    <div className="flex min-h-screen">
      {/* 왼쪽: 브랜드 */}
      <div className="flex flex-1 flex-col items-center justify-center gap-3 bg-[#0f172a] px-10 relative overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(99,102,241,0.15),transparent_50%)]" />
        <div className="relative z-10 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-500 text-2xl shadow-[0_8px_24px_rgba(99,102,241,0.4)]">
          🛡
        </div>
        <p className="relative z-10 text-xl font-bold tracking-tight text-white">Vibe Admin</p>
        <div className="relative z-10 h-0.5 w-8 rounded-full bg-gradient-to-r from-indigo-500 to-violet-500" />
        <p className="relative z-10 text-center text-sm leading-relaxed text-slate-500">
          쇼핑몰 관리자 콘솔<br />인가된 운영팀만 접근 가능합니다
        </p>
        <div className="relative z-10 mt-6 flex gap-2">
          {['SUPER', 'ADMIN', 'OPS'].map((role) => (
            <span
              key={role}
              className="rounded-full border border-indigo-500/25 bg-indigo-500/15 px-2.5 py-1 text-[10px] font-semibold tracking-wide text-indigo-400"
            >
              {role}
            </span>
          ))}
        </div>
      </div>

      {/* 오른쪽: 폼 */}
      <div className="flex flex-[1.1] flex-col justify-center px-12 py-14">
        <p className="mb-2 text-[11px] font-bold uppercase tracking-widest text-indigo-500">Admin Console</p>
        <h1 className="mb-1.5 text-2xl font-extrabold tracking-tight text-slate-900">로그인</h1>
        <p className="mb-9 text-sm text-slate-400">계속하려면 관리자 계정으로 로그인하세요.</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="loginId" className="text-xs font-semibold text-slate-700">아이디</Label>
            <Input
              id="loginId"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              placeholder="관리자 아이디 입력"
              autoComplete="username"
              required
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="password" className="text-xs font-semibold text-slate-700">비밀번호</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력"
              autoComplete="current-password"
              required
            />
          </div>

          {errorMessage && (
            <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm text-red-500">
              ⚠️ {errorMessage}
            </div>
          )}

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-1 h-12 w-full rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 text-sm font-bold shadow-[0_4px_16px_rgba(99,102,241,0.35)] hover:opacity-90"
          >
            {mutation.isPending ? '로그인 중...' : '로그인'}
          </Button>
        </form>
      </div>
    </div>
  )
}

export default AdminLoginForm
