export type Role = 'ADMIN' | 'HR' | 'EMPLOYEE'

export interface AuthUser {
  accessToken: string
  tokenType: string
  email: string
  role: Role
  employeeId?: number
  fullName?: string
}

export interface Employee {
  id: number
  employeeCode: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  departmentId?: number
  departmentName?: string
  designation?: string
  employmentStatus: string
  joiningDate: string
  salary?: number
  profileImageUrl?: string
}

export interface Department {
  id: number
  name: string
  code: string
  description?: string
  headId?: number
  headName?: string
  employeeCount: number
  active: boolean
}

export interface DashboardStats {
  totalEmployees: number
  activeEmployees: number
  departmentCount: number
  todayPresent: number
  todayAbsent: number
  todayLate: number
  pendingLeaveRequests: number
  departmentDistribution: { name: string; count: number }[]
  recentEmployees: { id: number; name: string; code: string; department?: string; joiningDate: string }[]
}

export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  timestamp?: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
