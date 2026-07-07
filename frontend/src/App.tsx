import { useEffect, useState } from 'react';
import { api, type Station } from './api/client';
import { StationList } from './components/StationList';
import { RegisterStationForm } from './components/RegisterStationForm';

export function App() {
  const [stations, setStations] = useState<Station[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  async function refresh() {
    try {
      setError(null);
      setStations(await api.listStations());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load stations');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function reserve(id: string) {
    try {
      await api.reserve(id);
      refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Reserve failed');
    }
  }

  async function release(id: string) {
    try {
      await api.release(id);
      refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Release failed');
    }
  }

  return (
    <div className="app">
      <header>
        <h1>⚡ Volt-Grid</h1>
        <p className="muted">EV charging station network</p>
      </header>
      <RegisterStationForm onRegistered={refresh} />
      {error && <p className="error" role="alert">{error}</p>}
      {loading ? (
        <p>Loading stations…</p>
      ) : (
        <StationList stations={stations} onReserve={reserve} onRelease={release} />
      )}
    </div>
  );
}
