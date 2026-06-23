export interface ApiResponse<T> { success: boolean; message: string; data: T }
export interface Building { id?: number; name: string; description?: string }
export interface Floor { id?: number; buildingId: number; floorNo: number; name: string }
export interface Room { id?: number; floorId: number; roomNo: string; sortOrder: number }
export interface Bed { id?: number; roomId?: number; bedNo: string; studentId?: number | null; studentName?: string; studentNo?: string }
export interface Student { id?: number; studentNo: string; name: string; gender?: string; phone?: string; college?: string; major?: string; className?: string }
export interface StudentListItem extends Student { buildingName?: string | null; floorName?: string | null; roomNo?: string | null; bedNo?: string | null; dormitoryText: string }
export interface RoomMapItem { id: number; roomNo: string; sortOrder: number; totalBeds: number; occupiedBeds: number; emptyBeds: number; status: 'FULL' | 'AVAILABLE' }
export interface StudentDormitory { student: Student; building?: Building | null; floor?: Floor | null; room?: Room | null; bed?: Bed | null }
