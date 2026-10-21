import { useQuery } from '@tanstack/react-query'
import api from '../services/api'
import type { ApiResponse, Department } from '../types'

export default function DepartmentsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['departments'],
    queryFn: async () => {
      const res = await api.get<ApiResponse<Department[]>>('/departments')
      return res.data.data
    },
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Departments</h1>
        <p className="text-slate-500 mt-1">Organization structure</p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-slate-200 dark:bg-slate-700 rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {data?.map((dept) => (
            <div
              key={dept.id}
              className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm"
            >
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold">{dept.name}</h3>
                  <p className="text-xs text-slate-500 font-mono mt-0.5">{dept.code}</p>
                </div>
                <span
                  className={`text-xs px-2 py-0.5 rounded-full ${
                    dept.active ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {dept.active ? 'Active' : 'Inactive'}
                </span>
              </div>
              {dept.description && (
                <p className="text-sm text-slate-500 mt-2 line-clamp-2">{dept.description}</p>
              )}
              <div className="mt-4 flex items-center justify-between text-sm">
                <span className="text-slate-500">
                  Head: {dept.headName || '—'}
                </span>
                <span className="font-medium">{dept.employeeCount} employees</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
