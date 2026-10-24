
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export type Restaurant = {
  id: number
  name: string
  description: string
  address: string
  phoneNumber: string
  cuisineType: string
  open: boolean
  createdAt: string
}

export async function getRestaurants(): Promise<Restaurant[]> {
  const response = await fetch(`${API_BASE_URL}/api/restaurants`)

  if (!response.ok) {
    throw new Error(`Failed to load restaurants (${response.status})`)
  }

  return response.json() as Promise<Restaurant[]>
}
