import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Instruments from './pages/Instruments'
import Feeds from './pages/Feeds'
import Subscriptions from './pages/Subscriptions'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="instruments" element={<Instruments />} />
        <Route path="feeds" element={<Feeds />} />
        <Route path="subscriptions" element={<Subscriptions />} />
      </Route>
    </Routes>
  )
}

export default App
