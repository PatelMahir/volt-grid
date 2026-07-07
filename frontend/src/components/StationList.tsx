import type { Station } from '../api/client';

interface Props {
  stations: Station[];
  onReserve: (id: string) => void;
  onRelease: (id: string) => void;
}

export function StationList({ stations, onReserve, onRelease }: Props) {
  if (stations.length === 0) {
    return <p>No charging stations registered yet.</p>;
  }
  return (
    <div className="station-grid">
      {stations.map((s) => (
        <article className="station-card" key={s.id}>
          <div className="station-head">
            <h3>{s.name}</h3>
            <span className={`status status-${s.status}`}>{s.status}</span>
          </div>
          <p className="muted"><code>{s.externalId}</code></p>
          <dl className="specs">
            <div><dt>Connector</dt><dd>{s.connectorType ?? '—'}</dd></div>
            <div><dt>Power</dt><dd>{s.powerKw != null ? `${s.powerKw} kW` : '—'}</dd></div>
            <div><dt>Price</dt><dd>{s.pricePerKwh != null ? `$${s.pricePerKwh}/kWh` : '—'}</dd></div>
          </dl>
          <div className="actions">
            <button onClick={() => onReserve(s.externalId)} disabled={s.status !== 'AVAILABLE'}>
              Reserve
            </button>
            <button onClick={() => onRelease(s.externalId)} disabled={s.status === 'AVAILABLE'}>
              Release
            </button>
          </div>
        </article>
      ))}
    </div>
  );
}
