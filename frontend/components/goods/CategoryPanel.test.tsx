import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CategoryPanel from './CategoryPanel'
import type { CategoryResponse } from '@/types/goods.types'

vi.mock('@/hooks/goods/useCategoryCreate', () => ({
  useCategoryCreate: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
}))

vi.mock('@/hooks/goods/useCategoryUpdate', () => ({
  useCategoryUpdate: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
}))

const mockCategory: CategoryResponse = {
  ctgNo: 1,
  upCtgNo: null,
  ctgLvl: '1',
  ctgNm: '의류',
  sortOrd: 1,
  useYn: 'Y',
  children: [],
}

describe('CategoryPanel', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => vi.clearAllMocks())

  it('create 모드에서 등록 폼을 표시한다', () => {
    render(
      <CategoryPanel mode="create" onClose={onClose} onSuccess={onSuccess} />
    )

    expect(screen.getByText('카테고리 등록')).toBeInTheDocument()
    expect(screen.getByLabelText('카테고리명')).toBeInTheDocument()
    expect(screen.getByLabelText('정렬순서')).toBeInTheDocument()
    expect(screen.queryByLabelText('사용여부')).not.toBeInTheDocument()
  })

  it('edit 모드에서 수정 폼을 표시하고 기존 값을 채운다', () => {
    render(
      <CategoryPanel mode="edit" category={mockCategory} onClose={onClose} onSuccess={onSuccess} />
    )

    expect(screen.getByText('카테고리 수정')).toBeInTheDocument()
    expect(screen.getByLabelText('카테고리명')).toHaveValue('의류')
    expect(screen.getByLabelText('사용여부')).toBeInTheDocument()
  })

  it('취소 버튼 클릭 시 onClose를 호출한다', async () => {
    render(
      <CategoryPanel mode="create" onClose={onClose} onSuccess={onSuccess} />
    )

    await userEvent.click(screen.getByText('취소'))
    expect(onClose).toHaveBeenCalledOnce()
  })
})
