'use client'

import { useRef } from 'react'

interface GoodsImageInputProps {
  files: File[]
  onChange: (files: File[]) => void
}

const MAX_IMAGES = 5

const GoodsImageInput = ({ files, onChange }: GoodsImageInputProps) => {
  const inputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = Array.from(e.target.files ?? [])
    const merged = [...files, ...selected].slice(0, MAX_IMAGES)
    onChange(merged)
    if (inputRef.current) inputRef.current.value = ''
  }

  const removeFile = (index: number) => {
    onChange(files.filter((_, i) => i !== index))
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">
          첫 번째 이미지가 대표 이미지로 설정됩니다. (최대 {MAX_IMAGES}장)
        </p>
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          disabled={files.length >= MAX_IMAGES}
          className="rounded border border-blue-500 px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 disabled:opacity-40"
        >
          + 이미지 추가
        </button>
        <input
          ref={inputRef}
          type="file"
          accept="image/*"
          multiple
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      {files.length === 0 && (
        <p className="text-sm text-slate-400">등록된 이미지가 없습니다.</p>
      )}

      <div className="flex flex-wrap gap-3">
        {files.map((file, i) => (
          <div key={i} className="relative">
            {i === 0 && (
              <span className="absolute left-1 top-1 rounded bg-blue-600 px-1 py-0.5 text-xs text-white">
                대표
              </span>
            )}
            <img
              src={URL.createObjectURL(file)}
              alt={file.name}
              className="h-24 w-24 rounded border border-slate-200 object-cover"
            />
            <button
              type="button"
              onClick={() => removeFile(i)}
              className="absolute right-1 top-1 rounded-full bg-white px-1 text-xs text-slate-500 shadow hover:text-red-500"
            >
              ✕
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

export default GoodsImageInput
