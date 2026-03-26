import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, AxiosHeaders } from 'axios'
import AdminLoginForm from './AdminLoginForm'
import { useAdminLogin } from '@/hooks/system/useAdminLogin'

vi.mock('@/hooks/system/useAdminLogin')

const mockMutate = vi.fn()
const mockUseAdminLogin = vi.mocked(useAdminLogin)

function setup(overrides: { isPending?: boolean; error?: Error | null } = {}) {
  mockUseAdminLogin.mockReturnValue({
    mutate: mockMutate,
    isPending: overrides.isPending ?? false,
    error: overrides.error ?? null,
  } as ReturnType<typeof useAdminLogin>)

  return render(<AdminLoginForm />)
}

function make401Error() {
  return new AxiosError(
    'Request failed with status code 401',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status: 401,
      data: {},
      statusText: 'Unauthorized',
      headers: new AxiosHeaders(),
      config: { headers: new AxiosHeaders() },
    },
  )
}

describe('AdminLoginForm', () => {
  beforeEach(() => {
    mockMutate.mockClear()
  })

  it('아이디와 비밀번호 입력 필드가 렌더링된다', () => {
    setup()
    expect(screen.getByLabelText('아이디')).toBeInTheDocument()
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument()
  })

  it('아이디 입력 필드에 required 속성이 있다', () => {
    setup()
    expect(screen.getByLabelText('아이디')).toBeRequired()
  })

  it('비밀번호 입력 필드에 required 속성이 있다', () => {
    setup()
    expect(screen.getByLabelText('비밀번호')).toBeRequired()
  })

  it('아이디와 비밀번호 입력 후 제출하면 login mutation이 호출된다', async () => {
    setup()
    await userEvent.type(screen.getByLabelText('아이디'), 'admin')
    await userEvent.type(screen.getByLabelText('비밀번호'), 'password123')
    await userEvent.click(screen.getByRole('button', { name: '로그인' }))

    expect(mockMutate).toHaveBeenCalledWith({ loginId: 'admin', password: 'password123' })
  })

  it('로그인 중일 때 버튼이 비활성화된다', () => {
    setup({ isPending: true })
    expect(screen.getByRole('button', { name: '로그인 중...' })).toBeDisabled()
  })

  it('401 에러 시 아이디 또는 비밀번호가 올바르지 않습니다 메시지가 표시된다', () => {
    setup({ error: make401Error() })
    expect(screen.getByText(/아이디 또는 비밀번호가 올바르지 않습니다/)).toBeInTheDocument()
  })

  it('기타 에러 시 잠시 후 다시 시도해주세요 메시지가 표시된다', () => {
    setup({ error: new Error('Internal Server Error') })
    expect(screen.getByText(/잠시 후 다시 시도해주세요/)).toBeInTheDocument()
  })
})
