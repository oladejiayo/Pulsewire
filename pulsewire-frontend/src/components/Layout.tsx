import { Outlet, NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  LineChart, 
  Rss, 
  Link2,
  Activity 
} from 'lucide-react';

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/instruments', icon: LineChart, label: 'Instruments' },
  { to: '/feeds', icon: Rss, label: 'Feeds' },
  { to: '/subscriptions', icon: Link2, label: 'Subscriptions' },
];

export default function Layout() {
  return (
    <div className="min-h-screen bg-slate-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900 text-white flex flex-col">
        {/* Logo */}
        <div className="p-6 border-b border-slate-700">
          <div className="flex items-center gap-3">
            <Activity className="w-8 h-8 text-pulse-400" />
            <div>
              <h1 className="text-xl font-bold">PulseWire</h1>
              <p className="text-xs text-slate-400">Market Data Platform</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {navItems.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      isActive
                        ? 'bg-pulse-600 text-white'
                        : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                    }`
                  }
                >
                  <item.icon className="w-5 h-5" />
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        {/* Footer */}
        <div className="p-4 border-t border-slate-700 text-xs text-slate-500">
          <p>© 2026 PulseWire</p>
          <p>v0.0.1-SNAPSHOT</p>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
