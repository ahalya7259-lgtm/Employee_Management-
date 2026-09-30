import { useQuery } from '@tanstack/react-query'
import api from '../services/api'
import type { ApiResponse, DashboardStats } from '../types'
import { Users, Building2, UserCheck, Clock, FileText } from 'lucide-react'

export default function DashboardPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: async () => {
      const res = await api.get<ApiResponse<DashboardStats>>('/dashboard/stats')
      return res.data.data
    },
  })

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 bg-slate-200 dark:bg-slate-700 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  if (error || !data) {
    return <div className="text-red-600">Failed to load dashboard stats.</div>
  }

  const cards = [
    { label: 'Total Employees', value: data.totalEmployees, icon: Users, color: 'bg-blue-500' },
    { label: 'Active Employees', value: data.activeEmployees, icon: UserCheck, color: 'bg-green-500' },
    { label: 'Departments', value: data.departmentCount, icon: Building2, color: 'bg-purple-500' },
    { label: "Today's Present", value: data.todayPresent, icon: Clock, color: 'bg-amber-500' },
    { label: 'Pending Leaves', value: data.pendingLeaveRequests, icon: FileText, color: 'bg-rose-500' },
  ]

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-slate-500 mt-1">Overview of your organization</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
        {cards.map((card) => (
          <div
            key={card.label}
            className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-500">{card.label}</p>
                <p className="text-2xl font-bold mt-1">{card.value}</p>
              </div>
              <div className={`${card.color} p-3 rounded-lg text-white`}>
                <card.icon className="h-5 w-5" />
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <h2 className="font-semibold mb-4">Department Distribution</h2>
          {data.departmentDistribution.length === 0 ? (
            <p className="text-slate-500 text-sm">No data</p>
          ) : (
            <ul className="space-y-3">
              {data.departmentDistribution.map((d) => (
                <li key={d.name} className="flex items-center justify-between">
                  <span className="text-sm">{d.name}</span>
                  <span className="text-sm font-medium bg-slate-100 dark:bg-slate-700 px-2.5 py-0.5 rounded-full">
                    {d.count}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
          <h2 className="font-semibold mb-4">Recent Employees</h2>
          {data.recentEmployees.length === 0 ? (
            <p className="text-slate-500 text-sm">No data</p>
          ) : (
            <ul className="space-y-3">
              {data.recentEmployees.map((e) => (
                <li key={e.id} className="flex items-center justify-between text-sm">
                  <div>
                    <p className="font-medium">{e.name}</p>
                    <p className="text-slate-500">{e.code} · {e.department || '—'}</p>
                  </div>
                  <span className="text-slate-400">{e.joiningDate}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
