import React, { useEffect, useState } from 'react';

type Contract = {
  id: string;
  name: string;
  version: string;
  status: string;
};

export function App() {
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch('/api/v1/contracts')
      .then(r => r.json())
      .then(setContracts)
      .catch(e => setError(String(e)));
  }, []);

  return (
    <div style={{ fontFamily: 'system-ui', padding: 32, maxWidth: 1100, margin: '0 auto' }}>
      <h1 style={{ margin: 0 }}>API SENTINEL</h1>
      <p style={{ color: '#666' }}>API Intelligence &amp; Contract Governance</p>

      <div style={{ display: 'flex', gap: 24, margin: '24px 0' }}>
        <Stat label="APIs" value={contracts.length} />
        <Stat label="Breaking Changes" value={0} />
        <Stat label="Risk" value={'OK'} />
      </div>

      <h2>Registered Contracts</h2>
      {error && <div style={{ color: '#c00' }}>Error: {error}</div>}
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
            <th>Name</th><th>Version</th><th>Status</th><th>ID</th>
          </tr>
        </thead>
        <tbody>
          {contracts.map(c => (
            <tr key={c.id} style={{ borderBottom: '1px solid #eee' }}>
              <td>{c.name}</td>
              <td>{c.version}</td>
              <td>{c.status}</td>
              <td style={{ fontFamily: 'monospace', color: '#999' }}>{c.id}</td>
            </tr>
          ))}
          {contracts.length === 0 && !error && (
            <tr><td colSpan={4} style={{ color: '#999', padding: 16 }}>No contracts registered yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, minWidth: 160 }}>
      <div style={{ color: '#666', fontSize: 12 }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 600 }}>{value}</div>
    </div>
  );
}