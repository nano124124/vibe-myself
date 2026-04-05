'use client'

interface GoodsImageInputProps {
  imgUrls: string[]
  onChange: (imgUrls: string[]) => void
}

const MAX_IMAGES = 5

const GoodsImageInput = ({ imgUrls, onChange }: GoodsImageInputProps) => {
  const addUrl = () => onChange([...imgUrls, ''])

  const updateUrl = (index: number, value: string) => {
    const next = [...imgUrls]
    next[index] = value
    onChange(next)
  }

  const removeUrl = (index: number) => onChange(imgUrls.filter((_, i) => i !== index))

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">첫 번째 이미지가 대표 이미지로 설정됩니다. (최대 {MAX_IMAGES}장)</p>
        <button
          type="button"
          onClick={addUrl}
          disabled={imgUrls.length >= MAX_IMAGES}
          className="rounded border border-blue-500 px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 disabled:opacity-40"
        >
          + 이미지 추가
        </button>
      </div>

      {imgUrls.map((url, i) => (
        <div key={i} className="flex items-center gap-2">
          <span className="w-5 shrink-0 text-center text-xs text-slate-400">{i + 1}</span>
          <input
            type="text"
            value={url}
            onChange={(e) => updateUrl(i, e.target.value)}
            placeholder="이미지 URL을 입력하세요"
            className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
          <button
            type="button"
            onClick={() => removeUrl(i)}
            className="shrink-0 text-slate-400 hover:text-red-500"
          >
            ✕
          </button>
        </div>
      ))}

      {imgUrls.length === 0 && (
        <p className="text-sm text-slate-400">등록된 이미지가 없습니다.</p>
      )}
    </div>
  )
}

export default GoodsImageInput