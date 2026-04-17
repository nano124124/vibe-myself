import { useQuery } from '@tanstack/react-query'
import { getCodeList } from '@/api/system.api'
import type { CodeResponse } from '@/types/system.types'

const EMPTY_CODES: CodeResponse[] = []

export const useCodeList = (codeGrpCd: string) =>
  useQuery({
    queryKey: ['system', 'codes', codeGrpCd],
    queryFn: () => getCodeList(codeGrpCd),
    staleTime: 1000 * 60 * 10, // 공통코드는 10분 캐시
  })
