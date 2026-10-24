
import { useEffect, useState } from 'react'
import { getRestaurants, type Restaurant } from './api'
import './App.css'

function App() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadRestaurants() {
      try {
        const data = await getRestaurants()
        setRestaurants(data)
      } catch (err) {
        setError(
          err instanceof Error ? err.message : 'Unable to load restaurants',
        )
      } finally {
        setLoading(false)
      }
    }

    void loadRestaurants()
  }, [])

  return (
    <div className="app">
      <header className="navbar">
        <a className="brand" href="/">
          <span className="brand-icon">🍽️</span>
          Food<span>Express</span>
        </a>
        <span>📍 Bengaluru</span>
      </header>

      <main className="restaurants-section">
        <span className="eyebrow">FRESH FROM YOUR BACKEND</span>
        <h1>Explore restaurants</h1>
        <p>Discover restaurants available in your area.</p>

        {loading && <p>Loading restaurants...</p>}

        {error && (
          <p role="alert">
            Could not load restaurants: {error}
          </p>
        )}

        {!loading && !error && restaurants.length === 0 && (
          <p>No restaurants found.</p>
        )}

        <div className="restaurant-grid">
          {restaurants.map((restaurant) => (
            <article className="restaurant-card" key={restaurant.id}>
              <div className="restaurant-info">
                <div className="restaurant-title">
                  <h2>{restaurant.name}</h2>
                  <span className="rating">
                    {restaurant.open ? '● Open' : '● Closed'}
                  </span>
                </div>

                <p>{restaurant.description}</p>
                <p>🍴 {restaurant.cuisineType}</p>
                <p>📍 {restaurant.address}</p>
                <p>📞 {restaurant.phoneNumber}</p>
              </div>
            </article>
          ))}
        </div>
      </main>
    </div>
  )
}

export default App
