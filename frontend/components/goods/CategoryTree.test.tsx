import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CategoryTree from './CategoryTree'
import type { CategoryResponse } from '@/types/goods.types'

const mockCategories: CategoryResponse[] = [
  {
    ctgNo: 1,
    upCtgNo: null,
    ctgLvl: '1',
    ctgNm: '의류',
    sortOrd: 1,
    useYn: 'Y',
    children: [
      {
        ctgNo: 11,
        upCtgNo: 1,
        ctgLvl: '2',
        ctgNm: '상의',
        sortOrd: 1,
        useYn: 'Y',
        children: [
          {
            ctgNo: 111,
            upCtgNo: 11,
            ctgLvl: '3',
            ctgNm: '반팔',
            sortOrd: 1,
            useYn: 'N',
            children: [],
          },
        ],
      },
    ],
  },
]

describe('CategoryTree', () => {
  const onEdit = vi.fn()
  const onCreateChild = vi.fn()

  it('카테고리 트리를 렌더링한다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    expect(screen.getByText('의류')).toBeInTheDocument()
    expect(screen.getByText('상의')).toBeInTheDocument()
    expect(screen.getByText('반팔')).toBeInTheDocument()
  })

  it('사용여부를 표시한다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    // 반팔(미사용)
    expect(screen.getByTestId('use-yn-111')).toHaveTextContent('N')
  })

  it('수정 버튼 클릭 시 onEdit을 호출한다', async () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    await userEvent.click(screen.getByTestId('edit-btn-1'))
    expect(onEdit).toHaveBeenCalledWith(mockCategories[0])
  })

  it('하위 추가 버튼 클릭 시 onCreateChild를 호출한다', async () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    await userEvent.click(screen.getByTestId('add-child-btn-1'))
    expect(onCreateChild).toHaveBeenCalledWith(1)
  })

  it('3단계 카테고리(ctgLvl=3)에는 하위 추가 버튼이 없다', () => {
    render(<CategoryTree categories={mockCategories} onEdit={onEdit} onCreateChild={onCreateChild} />)

    expect(screen.queryByTestId('add-child-btn-111')).not.toBeInTheDocument()
  })
})
