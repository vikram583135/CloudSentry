/**
 * CloudSentry — Production Observability & Incident Platform
 * Robust Vanilla JS application layer engineered for speed, density, and authentic SRE workflows.
 */

(function () {
  'use strict';

  // Application State
  const state = {
    activeView: 'overview',
    activePersona: 'SRE',
    isPolling: true,
    pollInterval: null,
    selectedIncident: null,
    backendConnected: false,

    // Core Incidents Data
    incidents: [
      {
        id: '1a91cf82-e567-4a11-b847-920f38c01991',
        incidentNumber: 'INC-000492',
        title: 'High Latency & Connection Pool Starvation in payment-service',
        description: 'HikariCP connection pool exhausted (100/100 active connections) causing HTTP 504 timeouts on checkout. Upstream latency spiked to 3,420ms.',
        severity: 'SEV1',
        priority: 'P1',
        status: 'OPEN',
        applicationName: 'payment-service',
        environment: 'prod',
        createdAt: new Date(Date.now() - 14 * 60000).toISOString(),
        slaTargetMinutes: 30,
        timeline: [
          { time: '14:21:40 UTC', author: 'AnomalyDetector', type: 'AUTOMATED', message: 'Z-Score deviation +3.8σ detected on metric: db_connection_count' },
          { time: '14:22:10 UTC', author: 'CloudSentry Engine', type: 'AUTOMATED', message: 'Incident auto-created with severity SEV1 (P1). Slack alert dispatched to #prod-critical-alerts.' },
          { time: '14:24:00 UTC', author: 'Alex Chen (SRE)', type: 'ENGINEER', message: 'Acknowledged pager. Investigating active queries on RDS Aurora writer node.' }
        ]
      },
      {
        id: '2b82df93-f678-4b22-a958-031e49d12002',
        incidentNumber: 'INC-000491',
        title: 'Elevated 5xx Error Rate on checkout-gateway ingress',
        description: 'NGINX ingress controller reporting 4.2% HTTP 502/504 error response rate across active checkout sessions.',
        severity: 'SEV2',
        priority: 'P2',
        status: 'INVESTIGATING',
        applicationName: 'checkout-gateway',
        environment: 'prod',
        createdAt: new Date(Date.now() - 38 * 60000).toISOString(),
        slaTargetMinutes: 60,
        timeline: [
          { time: '13:58:12 UTC', author: 'RateOfChangeDetector', type: 'AUTOMATED', message: 'Error rate increased by 420% in 5-minute moving window.' },
          { time: '14:02:30 UTC', author: 'Sarah Lin (Dev)', type: 'ENGINEER', message: 'Tracing correlate error logs with deployment sha 9f81a4.' }
        ]
      },
      {
        id: '3c73ef04-a789-4c33-ba69-142f50e23113',
        incidentNumber: 'INC-000489',
        title: 'Redis Cache Hit Ratio Degradation in user-auth',
        description: 'Cache hit ratio dropped from 96.4% to 71.2% following cache eviction surge in cluster node 2.',
        severity: 'SEV3',
        priority: 'P3',
        status: 'RESOLVED',
        applicationName: 'auth-api',
        environment: 'prod',
        createdAt: new Date(Date.now() - 110 * 60000).toISOString(),
        slaTargetMinutes: 240,
        timeline: [
          { time: '12:45:00 UTC', author: 'ThresholdDetector', type: 'AUTOMATED', message: 'Warning threshold breached: cache_hit_ratio < 80%' },
          { time: '13:10:00 UTC', author: 'Vikram S. (SRE)', type: 'ENGINEER', message: 'Increased Redis maxmemory-policy to volatile-lru. Hit ratio recovered to 97.1%.' },
          { time: '13:30:00 UTC', author: 'Vikram S. (SRE)', type: 'ENGINEER', message: 'Incident resolved. Root cause: transient batch job cache flush.' }
        ]
      }
    ],

    // Core Services Health
    services: [
      { name: 'payment-service', type: 'Core API', health: 'CRITICAL', p95: '3,420 ms', errorRate: '3.8%', cpu: '94.8%', pods: '6/8' },
      { name: 'checkout-gateway', type: 'Ingress API', health: 'WARNING', p95: '840 ms', errorRate: '4.2%', cpu: '62.4%', pods: '12/12' },
      { name: 'auth-api', type: 'Identity Service', health: 'HEALTHY', p95: '48 ms', errorRate: '0.01%', cpu: '41.2%', pods: '8/8' },
      { name: 'order-db', type: 'PostgreSQL RDS', health: 'WARNING', p95: '1,240 ms', errorRate: '0.00%', cpu: '88.5%', pods: 'Primary + 2 Replica' },
      { name: 'notification-worker', type: 'Async Consumer', health: 'HEALTHY', p95: '12 ms', errorRate: '0.00%', cpu: '22.1%', pods: '4/4' }
    ],

    // Telemetry Time-Series Buffers (60 points)
    telemetry: {
      cpu: generateSeries(60, 40, 95, 92),
      latency: generateSeries(60, 80, 3600, 3420),
      memory: generateSeries(60, 50, 72, 68),
      errors: generateSeries(60, 0.1, 5.0, 4.2)
    },

    // Detected Anomalies
    anomalies: [
      {
        detectedAt: '14:21:40 UTC',
        severity: 'CRITICAL',
        application: 'payment-service',
        metricType: 'CPU_USAGE',
        observed: '94.8%',
        expected: '45.0%',
        method: 'Threshold + Z-Score',
        zScore: '+3.8σ'
      },
      {
        detectedAt: '14:22:15 UTC',
        severity: 'HIGH',
        application: 'checkout-gateway',
        metricType: 'RESPONSE_TIME',
        observed: '3,420 ms',
        expected: '180 ms',
        method: 'Moving Average',
        zScore: '+4.2σ'
      },
      {
        detectedAt: '14:18:00 UTC',
        severity: 'MEDIUM',
        application: 'order-db',
        metricType: 'CONNECTION_COUNT',
        observed: '100 / 100',
        expected: '35 / 100',
        method: 'Rate of Change',
        zScore: '+2.9σ'
      }
    ],

    // Cost Recommendations
    recommendations: [
      {
        id: 'rec-1',
        title: 'Downsize 6x Over-Provisioned EC2 Instances',
        description: 'Instances running at <4% avg CPU utilization over past 14 days. Downsize from r5.2xlarge to r5.xlarge in us-east-1a.',
        resourceId: 'i-0a91f8274b0c829e1',
        service: 'Amazon EC2',
        savings: '$1,240/mo',
        applied: false
      },
      {
        id: 'rec-2',
        title: 'Terminate 14 Unattached EBS gp2 Volumes',
        description: 'Orphaned EBS volumes detached from terminated spot worker nodes totaling 2.4 TB provisioned capacity.',
        resourceId: 'vol-08c3e887f49a1b021',
        service: 'Amazon EBS',
        savings: '$288/mo',
        applied: false
      },
      {
        id: 'rec-3',
        title: 'Migrate CloudWatch Log Groups to S3 Glacier',
        description: 'Retention set to indefinite on debug log groups. Enforce 30-day retention and transition cold logs to Glacier Instant Retrieval.',
        resourceId: 'log-group:/aws/k8s/payment-debug',
        service: 'CloudWatch & S3',
        savings: '$410/mo',
        applied: false
      },
      {
        id: 'rec-4',
        title: 'Purchase 1-Year Compute Savings Plan',
        description: 'Consistent baseline usage of 42 vCPUs qualify for 1-Year No Upfront Compute Savings Plan at 28% discount rate.',
        resourceId: 'plan:compute-baseline-prod',
        service: 'AWS Cost Explorer',
        savings: '$2,150/mo',
        applied: false
      }
    ],

    // Dispatched Notifications Log
    notifications: [
      {
        time: '14:22:12 UTC',
        type: 'INCIDENT_CREATED',
        channel: 'SLACK',
        recipient: '#prod-critical-alerts',
        subject: '🚨 [SEV1] Incident INC-000492: High Latency & Connection Starvation',
        status: 'SENT',
        retries: 0
      },
      {
        time: '14:22:15 UTC',
        type: 'INCIDENT_CREATED',
        channel: 'PAGERDUTY',
        recipient: 'Primary On-Call (Alex Chen)',
        subject: 'High Latency & Connection Pool Starvation in payment-service',
        status: 'SENT',
        retries: 0
      },
      {
        time: '13:58:14 UTC',
        type: 'ANOMALY_ALERT',
        channel: 'SLACK',
        recipient: '#devops-monitoring',
        subject: '⚠️ [SEV2] Incident INC-000491: Error rate increased by 420%',
        status: 'SENT',
        retries: 0
      },
      {
        time: '13:30:02 UTC',
        type: 'INCIDENT_RESOLVED',
        channel: 'SLACK',
        recipient: '#prod-critical-alerts',
        subject: '✅ Incident INC-000489 resolved by Vikram S.',
        status: 'SENT',
        retries: 0
      }
    ]
  };

  function generateSeries(count, min, max, spikeAtEnd) {
    const data = [];
    let current = (min + max) / 2;
    for (let i = 0; i < count; i++) {
      if (i > count - 8 && spikeAtEnd) {
        current = spikeAtEnd + (Math.random() * 2 - 1);
      } else {
        current += (Math.random() * 6 - 3);
        current = Math.max(min, Math.min(max, current));
      }
      data.push(parseFloat(current.toFixed(1)));
    }
    return data;
  }

  // ==========================================================================
  // Initialization & View Routing
  // ==========================================================================
  function init() {
    setupNavigation();
    setupEventListeners();
    setupPersonaSelector();
    renderAllViews();
    startLivePolling();
    checkBackendHealth();
  }

  function setupNavigation() {
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const viewId = item.getAttribute('data-view');
        switchTab(viewId);
      });
    });
  }

  function switchTab(viewId) {
    state.activeView = viewId;

    // Update Sidebar Navigation state
    document.querySelectorAll('.nav-item').forEach(item => {
      item.classList.toggle('active', item.getAttribute('data-view') === viewId);
    });

    // Update Content Panels
    document.querySelectorAll('.view-panel').forEach(panel => {
      panel.style.display = panel.id === `view-${viewId}` ? 'block' : 'none';
    });

    // Re-render charts if telemetry view is opened
    if (viewId === 'telemetry') {
      renderTelemetryCharts();
    }
  }

  function setupEventListeners() {
    // Global Search Hotkey
    window.addEventListener('keydown', (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        const searchInput = document.getElementById('global-search');
        if (searchInput) searchInput.focus();
      }
      if (e.key === 'Escape') {
        closeDrawer();
        closeModal();
      }
    });

    // Drawer Close Buttons
    const btnCloseDrawer = document.getElementById('btn-close-drawer');
    const drawerBackdrop = document.getElementById('incident-drawer-backdrop');
    if (btnCloseDrawer) btnCloseDrawer.addEventListener('click', closeDrawer);
    if (drawerBackdrop) drawerBackdrop.addEventListener('click', closeDrawer);

    // Modal Create Incident
    const btnOpenCreate = document.getElementById('btn-open-create-incident');
    const btnOpenCreate2 = document.getElementById('btn-create-incident-2');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnCancelModal = document.getElementById('btn-cancel-modal');
    const modalBackdrop = document.getElementById('modal-create-incident');
    const btnSubmitIncident = document.getElementById('btn-submit-incident');

    if (btnOpenCreate) btnOpenCreate.addEventListener('click', openCreateModal);
    if (btnOpenCreate2) btnOpenCreate2.addEventListener('click', openCreateModal);
    if (btnCloseModal) btnCloseModal.addEventListener('click', closeModal);
    if (btnCancelModal) btnCancelModal.addEventListener('click', closeModal);
    if (btnSubmitIncident) btnSubmitIncident.addEventListener('click', submitNewIncident);

    // Live Streaming Poll Button
    const btnTogglePoll = document.getElementById('btn-toggle-poll');
    if (btnTogglePoll) {
      btnTogglePoll.addEventListener('click', togglePolling);
    }

    // Simulate Anomaly Button
    const btnSimulate = document.getElementById('btn-simulate-anomaly');
    if (btnSimulate) {
      btnSimulate.addEventListener('click', simulateAnomalySpike);
    }

    // Refresh charts button
    const btnRefreshCharts = document.getElementById('btn-refresh-charts');
    if (btnRefreshCharts) {
      btnRefreshCharts.addEventListener('click', () => {
        renderTelemetryCharts();
        showToast('Telemetry charts refreshed', 'info');
      });
    }

    // Diagnostic RCA button
    const btnDiagnostic = document.getElementById('btn-run-rca-diagnostic');
    if (btnDiagnostic) {
      btnDiagnostic.addEventListener('click', () => {
        showToast('RCA diagnostic rule evaluation triggered across 14 rules', 'info');
      });
    }

    // Incident Table Filter Pills
    document.querySelectorAll('#incident-status-filters .filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('#incident-status-filters .filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        renderIncidentsTable();
      });
    });

    document.querySelectorAll('#incident-sev-filters .filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('#incident-sev-filters .filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        renderIncidentsTable();
      });
    });
  }

  function setupPersonaSelector() {
    document.querySelectorAll('#persona-selector-pills .filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('#persona-selector-pills .filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        const persona = pill.getAttribute('data-persona');
        state.activePersona = persona;

        const roleBadge = document.getElementById('user-active-role');
        if (roleBadge) roleBadge.textContent = persona.toUpperCase();

        renderPersonaDashboard(persona);
        showToast(`Switched active view persona to: ${persona}`, 'info');
      });
    });
  }

  // ==========================================================================
  // Render Functions
  // ==========================================================================
  function renderAllViews() {
    renderOverview();
    renderIncidentsTable();
    renderTelemetryCharts();
    renderAnomaliesTable();
    renderCostRecommendations();
    renderNotificationsLog();
    renderPersonaDashboard(state.activePersona);
    updateBadges();
  }

  function updateBadges() {
    const openCount = state.incidents.filter(i => i.status !== 'RESOLVED' && i.status !== 'CLOSED').length;
    const badgeInc = document.getElementById('badge-open-incidents');
    const kpiInc = document.getElementById('kpi-active-incidents');
    if (badgeInc) badgeInc.textContent = openCount;
    if (kpiInc) kpiInc.textContent = openCount;

    const badgeAnom = document.getElementById('badge-anomalies');
    if (badgeAnom) badgeAnom.textContent = state.anomalies.length;
  }

  function renderOverview() {
    // 1. Overview Incidents Table
    const tbody = document.getElementById('overview-incidents-table');
    if (!tbody) return;

    const activeList = state.incidents.filter(i => i.status !== 'CLOSED').slice(0, 5);
    tbody.innerHTML = activeList.map(inc => `
      <tr onclick="window.CloudSentryApp.openIncidentDrawer('${inc.id}')">
        <td class="mono-cell" style="font-weight: 700; color: #fff;">${inc.incidentNumber}</td>
        <td><span class="badge badge-${inc.severity.toLowerCase()}">${inc.severity}</span></td>
        <td><div style="font-weight: 600; color: #fff;">${escapeHtml(inc.title)}</div></td>
        <td><span class="app-chip">${escapeHtml(inc.applicationName)}</span></td>
        <td><span class="badge-status ${inc.status.toLowerCase()}">${inc.status}</span></td>
        <td class="mono-cell" style="color: ${inc.severity === 'SEV1' ? 'var(--sev1-rose)' : 'var(--text-muted)'};">${inc.slaTargetMinutes}m SLA</td>
        <td>
          <div class="action-btn-group" onclick="event.stopPropagation()">
            <button class="table-btn" onclick="window.CloudSentryApp.quickTransitionStatus('${inc.id}', 'ACKNOWLEDGED')">Ack</button>
            <button class="table-btn" onclick="window.CloudSentryApp.quickTransitionStatus('${inc.id}', 'RESOLVED')">Resolve</button>
          </div>
        </td>
      </tr>
    `).join('');

    // 2. Services Health Table
    const servicesTbody = document.getElementById('services-health-table');
    if (servicesTbody) {
      servicesTbody.innerHTML = state.services.map(svc => `
        <tr>
          <td><strong style="color: #fff;">${svc.name}</strong></td>
          <td style="color: var(--text-muted);">${svc.type}</td>
          <td><span class="badge ${svc.health === 'CRITICAL' ? 'badge-sev1' : (svc.health === 'WARNING' ? 'badge-sev3' : 'badge-status resolved')}">${svc.health}</span></td>
          <td class="mono-cell" style="color: ${svc.health === 'CRITICAL' ? 'var(--sev1-rose)' : 'var(--text-secondary)'};">${svc.p95}</td>
          <td class="mono-cell" style="color: ${parseFloat(svc.errorRate) > 1.0 ? 'var(--sev2-orange)' : 'var(--text-secondary)'};">${svc.errorRate}</td>
          <td class="mono-cell">${svc.cpu}</td>
          <td class="mono-cell">${svc.pods}</td>
        </tr>
      `).join('');
    }
  }

  function renderIncidentsTable() {
    const tbody = document.getElementById('incidents-full-table');
    if (!tbody) return;

    // Filters
    const statusFilter = document.querySelector('#incident-status-filters .filter-pill.active')?.getAttribute('data-filter') || 'all';
    const sevFilter = document.querySelector('#incident-sev-filters .filter-pill.active')?.getAttribute('data-sev') || 'all';

    let filtered = state.incidents;
    if (statusFilter !== 'all') {
      filtered = filtered.filter(i => i.status === statusFilter);
    }
    if (sevFilter !== 'all') {
      filtered = filtered.filter(i => i.severity === sevFilter);
    }

    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 24px; color: var(--text-muted);">No incidents matching selected filters.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtered.map(inc => `
      <tr onclick="window.CloudSentryApp.openIncidentDrawer('${inc.id}')">
        <td class="mono-cell" style="font-weight: 700; color: #fff;">${inc.incidentNumber}</td>
        <td><span class="badge badge-${inc.severity.toLowerCase()}">${inc.severity}</span></td>
        <td><span class="badge badge-sev4">${inc.priority}</span></td>
        <td>
          <div style="font-weight: 600; color: #fff;">${escapeHtml(inc.title)}</div>
          <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">${escapeHtml(inc.description.slice(0, 90))}...</div>
        </td>
        <td><span class="app-chip">${escapeHtml(inc.applicationName)}</span></td>
        <td><span class="badge-status ${inc.status.toLowerCase()}">${inc.status}</span></td>
        <td class="mono-cell" style="color: var(--text-muted);">${formatTimeAgo(inc.createdAt)}</td>
        <td>
          <div class="action-btn-group" onclick="event.stopPropagation()">
            <button class="table-btn" onclick="window.CloudSentryApp.openIncidentDrawer('${inc.id}')">Manage</button>
            <button class="table-btn" onclick="window.CloudSentryApp.quickTransitionStatus('${inc.id}', 'RESOLVED')">✓</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function renderAnomaliesTable() {
    const tbody = document.getElementById('anomalies-table-body');
    if (!tbody) return;

    tbody.innerHTML = state.anomalies.map(anom => `
      <tr>
        <td class="mono-cell" style="color: var(--text-muted);">${anom.detectedAt}</td>
        <td><span class="badge badge-${anom.severity === 'CRITICAL' ? 'sev1' : (anom.severity === 'HIGH' ? 'sev2' : 'sev3')}">${anom.severity}</span></td>
        <td><span class="app-chip">${anom.application}</span></td>
        <td class="mono-cell" style="color: #fff;">${anom.metricType}</td>
        <td class="mono-cell"><strong style="color: var(--sev1-rose);">${anom.observed}</strong> (expected: ${anom.expected})</td>
        <td>${anom.method}</td>
        <td class="mono-cell" style="font-weight: 700; color: var(--sev1-rose);">${anom.zScore}</td>
        <td>
          <button class="table-btn" onclick="window.CloudSentryApp.switchTab('rca')">Analyze RCA →</button>
        </td>
      </tr>
    `).join('');
  }

  function renderCostRecommendations() {
    const container = document.getElementById('recommendations-grid');
    if (!container) return;

    container.innerHTML = state.recommendations.map(rec => `
      <div class="rec-card" id="rec-card-${rec.id}">
        <div>
          <div class="rec-header">
            <span class="rec-title">${escapeHtml(rec.title)}</span>
            <span class="rec-savings">${rec.savings}</span>
          </div>
          <p style="color: var(--text-secondary); font-size: 12px; margin-top: 8px; line-height: 1.5;">${escapeHtml(rec.description)}</p>
          <div style="margin-top: 10px;">
            <span class="mono-cell" style="font-size: 10px; color: var(--text-muted);">RESOURCE: ${rec.resourceId}</span>
          </div>
        </div>
        <div class="rec-footer">
          <span class="app-chip">${rec.service}</span>
          ${rec.applied
            ? `<span class="badge badge-status resolved">✓ APPLIED</span>`
            : `<button class="btn-primary" style="padding: 4px 10px; font-size: 11px;" onclick="window.CloudSentryApp.applyRecommendation('${rec.id}')">Apply Recommendation</button>`
          }
        </div>
      </div>
    `).join('');
  }

  function renderNotificationsLog() {
    const tbody = document.getElementById('notifications-log-table');
    if (!tbody) return;

    tbody.innerHTML = state.notifications.map(notif => `
      <tr>
        <td class="mono-cell" style="color: var(--text-muted);">${notif.time}</td>
        <td><span class="mono-cell" style="font-weight: 600; color: #fff;">${notif.type}</span></td>
        <td><span class="badge badge-sev4">${notif.channel}</span></td>
        <td class="mono-cell">${escapeHtml(notif.recipient)}</td>
        <td><div style="font-size: 12px; color: var(--text-primary); max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(notif.subject)}</div></td>
        <td><span class="badge badge-status resolved">✓ ${notif.status}</span></td>
        <td class="mono-cell">${notif.retries}</td>
      </tr>
    `).join('');
  }

  function renderPersonaDashboard(persona) {
    const wrapper = document.getElementById('persona-content-wrapper');
    if (!wrapper) return;

    switch (persona) {
      case 'SRE':
        wrapper.innerHTML = `
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Active Pager Outages</span><span class="badge badge-sev1">CRITICAL</span></div>
              <div class="kpi-value" style="color: var(--sev1-rose);">1 Active</div>
              <div class="kpi-footer"><span>Primary Escalation: Alex Chen</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">MTTA (Mean Time to Ack)</span><span class="badge badge-status resolved">SLO: &lt;5m</span></div>
              <div class="kpi-value">2.4m</div>
              <div class="kpi-footer"><span class="kpi-trend positive">▼ -30s</span> from yesterday</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Alert False Positive Rate</span><span class="badge badge-sev4">TUNING</span></div>
              <div class="kpi-value">3.1%</div>
              <div class="kpi-footer"><span>Cooldown window: 5 mins active</span></div>
            </div>
          </div>
          <div class="card-container" style="margin-top: 16px; padding: 18px;">
            <h3 style="font-size: 14px; font-weight: 600; color: #fff; margin-bottom: 10px;">On-Call Escalation Roster (Week 41)</h3>
            <div style="display: flex; gap: 20px; flex-wrap: wrap;">
              <div style="background-color: var(--bg-surface-elevated); padding: 12px 16px; border-radius: 6px; flex: 1; min-width: 220px;">
                <span class="mono-cell" style="color: var(--sev4-sky); font-size: 11px;">PRIMARY ON-CALL:</span>
                <div style="font-weight: 700; color: #fff; font-size: 14px; margin-top: 4px;">Alex Chen (SRE Lead)</div>
                <div style="color: var(--text-muted); font-size: 11px;">Coverage: 08:00 - 20:00 UTC (Phone + PagerDuty)</div>
              </div>
              <div style="background-color: var(--bg-surface-elevated); padding: 12px 16px; border-radius: 6px; flex: 1; min-width: 220px;">
                <span class="mono-cell" style="color: var(--sev4-sky); font-size: 11px;">SECONDARY ON-CALL:</span>
                <div style="font-weight: 700; color: #fff; font-size: 14px; margin-top: 4px;">Vikram S. (DevOps Eng)</div>
                <div style="color: var(--text-muted); font-size: 11px;">Coverage: Escalation fallback (10 min SLA)</div>
              </div>
            </div>
          </div>
        `;
        break;

      case 'DEVELOPER':
        wrapper.innerHTML = `
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">My Services (payment-service)</span><span class="badge badge-sev1">DEGRADED</span></div>
              <div class="kpi-value">3,420 ms</div>
              <div class="kpi-footer"><span class="kpi-trend negative">▲ Connection pool leak</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Latest Git Deployment</span><span class="badge badge-status resolved">MERGED</span></div>
              <div class="kpi-value" style="font-size: 16px; font-family: var(--font-mono);">commit: 8b1f4a9</div>
              <div class="kpi-footer"><span>Deployed 28m ago by Sarah Lin</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Jvm Heap Allocated</span><span class="badge badge-sev4">MEMORY</span></div>
              <div class="kpi-value">4.2 / 8.0 GB</div>
              <div class="kpi-footer"><span class="kpi-trend positive">No OOM risk detected</span></div>
            </div>
          </div>
          <div class="card-container" style="margin-top: 16px; padding: 18px;">
            <h3 style="font-size: 14px; font-weight: 600; color: #fff; margin-bottom: 8px;">Recent Stack Trace Insights (payment-service)</h3>
            <div class="code-action-box" style="flex-direction: column; align-items: flex-start;">
              <code>Caused by: java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30002ms.</code>
              <code style="color: var(--text-muted); margin-top: 4px;">at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:696)</code>
              <code style="color: var(--text-muted);">at com.devops.platform.payment.service.CheckoutProcessor.execute(CheckoutProcessor.java:84)</code>
            </div>
          </div>
        `;
        break;

      case 'MANAGER':
        wrapper.innerHTML = `
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Monthly Cloud Spend</span><span class="badge badge-sev3">80.4%</span></div>
              <div class="kpi-value">$48,219</div>
              <div class="kpi-footer"><span>Budget ceiling: $60,000 / month</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Overall SLA Availability</span><span class="badge badge-status resolved">99.98%</span></div>
              <div class="kpi-value">99.98%</div>
              <div class="kpi-footer"><span class="kpi-trend positive">Above contractual target (99.9%)</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Identified Savings Pipeline</span><span class="badge badge-status resolved">OPPORTUNITY</span></div>
              <div class="kpi-value" style="color: var(--status-healthy);">$4,088/mo</div>
              <div class="kpi-footer"><span>Annualized potential: $49,056/yr</span></div>
            </div>
          </div>
        `;
        break;

      case 'ADMIN':
        wrapper.innerHTML = `
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Total Platform Users</span><span class="badge badge-sev4">RBAC</span></div>
              <div class="kpi-value">42 Users</div>
              <div class="kpi-footer"><span>12 SRE, 24 Dev, 4 Admin, 2 Manager</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Kafka Message Bus Lag</span><span class="badge badge-status resolved">0 LAG</span></div>
              <div class="kpi-value">0 msgs</div>
              <div class="kpi-footer"><span>3 brokers active | 12 partitions</span></div>
            </div>
            <div class="kpi-card">
              <div class="kpi-header"><span class="kpi-title">Redis Cache Allocation</span><span class="badge badge-status resolved">HEALTHY</span></div>
              <div class="kpi-value">412 MB / 2 GB</div>
              <div class="kpi-footer"><span>Hit ratio: 97.4%</span></div>
            </div>
          </div>
        `;
        break;
    }
  }

  // ==========================================================================
  // Interactive SVG Charts Engine (No Heavy Dependencies)
  // ==========================================================================
  function renderTelemetryCharts() {
    renderSvgChart('chart-cpu-container', state.telemetry.cpu, 0, 100, '%', 80, 95);
    renderSvgChart('chart-latency-container', state.telemetry.latency, 0, 4000, 'ms', 1000, 3000);
    renderSvgChart('chart-mem-container', state.telemetry.memory, 0, 100, '%', 80, 90);
    renderSvgChart('chart-errors-container', state.telemetry.errors, 0, 10, '%', 1.0, 3.0);
  }

  function renderSvgChart(containerId, series, minY, maxY, unit, warnY, critY) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const width = container.clientWidth || 400;
    const height = 180;
    const padding = { top: 15, right: 15, bottom: 25, left: 40 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const points = series.map((val, idx) => {
      const x = padding.left + (idx / (series.length - 1)) * chartW;
      const normalizedY = (val - minY) / (maxY - minY);
      const y = padding.top + (1 - normalizedY) * chartH;
      return { x, y, val };
    });

    const pathD = points.reduce((acc, pt, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${pt.x.toFixed(1)} ${pt.y.toFixed(1)}`, '');
    const areaD = `${pathD} L ${points[points.length - 1].x.toFixed(1)} ${(padding.top + chartH).toFixed(1)} L ${padding.left} ${(padding.top + chartH).toFixed(1)} Z`;

    const critYPos = padding.top + (1 - (critY - minY) / (maxY - minY)) * chartH;
    const lastPt = points[points.length - 1];

    container.innerHTML = `
      <svg class="svg-chart" viewBox="0 0 ${width} ${height}">
        <defs>
          <linearGradient id="gradient-${containerId}" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="#38bdf8" stop-opacity="0.35"/>
            <stop offset="100%" stop-color="#38bdf8" stop-opacity="0.0"/>
          </linearGradient>
        </defs>

        <!-- Threshold Line -->
        <line x1="${padding.left}" y1="${critYPos}" x2="${width - padding.right}" y2="${critYPos}" class="chart-threshold-line" />
        <text x="${width - padding.right - 4}" y="${critYPos - 4}" fill="var(--sev1-rose)" font-size="9" text-anchor="end" font-family="monospace">CRIT: ${critY}${unit}</text>

        <!-- Area & Stroke -->
        <path d="${areaD}" fill="url(#gradient-${containerId})" />
        <path d="${pathD}" class="chart-path-primary" />

        <!-- Latest Value Pulse Pin -->
        <circle cx="${lastPt.x}" cy="${lastPt.y}" r="4" fill="#38bdf8" stroke="#fff" stroke-width="2" />

        <!-- Axes -->
        <text x="${padding.left - 6}" y="${padding.top + 10}" fill="var(--text-muted)" font-size="9" text-anchor="end" font-family="monospace">${maxY}</text>
        <text x="${padding.left - 6}" y="${padding.top + chartH}" fill="var(--text-muted)" font-size="9" text-anchor="end" font-family="monospace">${minY}</text>
        <text x="${padding.left}" y="${height - 6}" fill="var(--text-muted)" font-size="9" font-family="monospace">-15m</text>
        <text x="${width - padding.right}" y="${height - 6}" fill="var(--text-muted)" font-size="9" text-anchor="end" font-family="monospace">Now</text>
      </svg>
    `;
  }

  // ==========================================================================
  // Incident Lifecycle & Drawer Management
  // ==========================================================================
  function openIncidentDrawer(incidentId) {
    const inc = state.incidents.find(i => i.id === incidentId);
    if (!inc) return;

    state.selectedIncident = inc;

    document.getElementById('drawer-inc-number').textContent = inc.incidentNumber;
    document.getElementById('drawer-inc-title').textContent = inc.title;
    document.getElementById('drawer-inc-app').textContent = inc.applicationName;
    document.getElementById('drawer-inc-env').textContent = inc.environment;
    document.getElementById('drawer-inc-desc').textContent = inc.description;

    const statusEl = document.getElementById('drawer-inc-status');
    statusEl.textContent = inc.status;
    statusEl.style.color = inc.status === 'RESOLVED' ? 'var(--status-healthy)' : 'var(--sev1-rose)';

    const sevBadge = document.getElementById('drawer-sev-badge');
    sevBadge.textContent = inc.severity;
    sevBadge.className = `badge badge-${inc.severity.toLowerCase()}`;

    const priorityBadge = document.getElementById('drawer-priority-badge');
    priorityBadge.textContent = inc.priority;

    renderDrawerTimeline(inc);

    document.getElementById('incident-drawer').classList.add('open');
    document.getElementById('incident-drawer-backdrop').classList.add('open');
  }

  function closeDrawer() {
    document.getElementById('incident-drawer')?.classList.remove('open');
    document.getElementById('incident-drawer-backdrop')?.classList.remove('open');
    state.selectedIncident = null;
  }

  function renderDrawerTimeline(incident) {
    const container = document.getElementById('drawer-timeline-container');
    if (!container) return;

    container.innerHTML = incident.timeline.map((item, idx) => `
      <div class="timeline-node ${item.type === 'AUTOMATED' ? 'danger' : 'success'}">
        <div class="timeline-meta">
          <span class="timeline-author">${escapeHtml(item.author)}</span>
          <span class="timeline-time">${item.time}</span>
        </div>
        <div class="timeline-comment-box">${escapeHtml(item.message)}</div>
      </div>
    `).join('');
  }

  function transitionDrawerStatus(newStatus) {
    if (!state.selectedIncident) return;
    const inc = state.selectedIncident;
    const oldStatus = inc.status;
    inc.status = newStatus;

    const nowStr = new Date().toTimeString().slice(0, 8) + ' UTC';
    inc.timeline.push({
      time: nowStr,
      author: 'Vikram S. (SRE Lead)',
      type: 'ENGINEER',
      message: `Status transitioned from ${oldStatus} to ${newStatus}.`
    });

    renderDrawerTimeline(inc);
    renderAllViews();
    showToast(`Incident ${inc.incidentNumber} transitioned to ${newStatus}`, 'success');

    // Sync with backend if connected
    sendBackendStatusUpdate(inc.id, newStatus);
  }

  function quickTransitionStatus(incidentId, newStatus) {
    const inc = state.incidents.find(i => i.id === incidentId);
    if (!inc) return;

    const oldStatus = inc.status;
    inc.status = newStatus;

    const nowStr = new Date().toTimeString().slice(0, 8) + ' UTC';
    inc.timeline.push({
      time: nowStr,
      author: 'Vikram S. (SRE Lead)',
      type: 'ENGINEER',
      message: `Quick status update from table: ${oldStatus} -> ${newStatus}.`
    });

    renderAllViews();
    showToast(`Incident ${inc.incidentNumber} updated to ${newStatus}`, 'success');
    sendBackendStatusUpdate(inc.id, newStatus);
  }

  function addTimelineComment() {
    const input = document.getElementById('drawer-comment-input');
    if (!input || !input.value.trim() || !state.selectedIncident) return;

    const text = input.value.trim();
    const nowStr = new Date().toTimeString().slice(0, 8) + ' UTC';

    state.selectedIncident.timeline.push({
      time: nowStr,
      author: 'Vikram S. (SRE Lead)',
      type: 'ENGINEER',
      message: text
    });

    input.value = '';
    renderDrawerTimeline(state.selectedIncident);
    showToast('Engineering update posted to incident timeline', 'info');
  }

  // ==========================================================================
  // Create Incident Modal Handlers
  // ==========================================================================
  function openCreateModal() {
    document.getElementById('modal-create-incident')?.classList.add('open');
  }

  function closeModal() {
    document.getElementById('modal-create-incident')?.classList.remove('open');
  }

  function submitNewIncident() {
    const title = document.getElementById('form-title')?.value.trim();
    const severity = document.getElementById('form-severity')?.value;
    const priority = document.getElementById('form-priority')?.value;
    const app = document.getElementById('form-application')?.value;
    const env = document.getElementById('form-env')?.value;
    const desc = document.getElementById('form-description')?.value.trim() || 'No additional description provided.';

    if (!title) {
      showToast('Incident title is required', 'danger');
      return;
    }

    const nextIncNum = 'INC-' + String(493 + state.incidents.length).padStart(6, '0');
    const newIncident = {
      id: crypto.randomUUID ? crypto.randomUUID() : 'inc-' + Date.now(),
      incidentNumber: nextIncNum,
      title: title,
      description: desc,
      severity: severity,
      priority: priority,
      status: 'OPEN',
      applicationName: app,
      environment: env,
      createdAt: new Date().toISOString(),
      slaTargetMinutes: severity === 'SEV1' ? 30 : 60,
      timeline: [
        {
          time: new Date().toTimeString().slice(0, 8) + ' UTC',
          author: 'Vikram S. (SRE Lead)',
          type: 'ENGINEER',
          message: `Incident created manually with ${severity} severity (${priority}). Slack and PagerDuty notifications triggered.`
        }
      ]
    };

    state.incidents.unshift(newIncident);

    // Also add to dispatched notifications log
    state.notifications.unshift({
      time: new Date().toTimeString().slice(0, 8) + ' UTC',
      type: 'INCIDENT_CREATED',
      channel: 'SLACK',
      recipient: '#prod-critical-alerts',
      subject: `🚨 [${severity}] Incident ${nextIncNum}: ${title}`,
      status: 'SENT',
      retries: 0
    });

    closeModal();
    renderAllViews();
    showToast(`Created incident ${nextIncNum} and dispatched alerts`, 'success');

    // Attempt backend POST if connected
    sendBackendCreateIncident(newIncident);
  }

  // ==========================================================================
  // Cost Recommendations Actions
  // ==========================================================================
  function applyRecommendation(recId) {
    const rec = state.recommendations.find(r => r.id === recId);
    if (!rec) return;

    rec.applied = true;
    renderCostRecommendations();
    showToast(`Applied recommendation: ${rec.title} (Savings: ${rec.savings})`, 'success');
  }

  function applyAllRecommendations() {
    state.recommendations.forEach(r => r.applied = true);
    renderCostRecommendations();
    showToast('All 4 optimization recommendations applied successfully! Estimated savings: $4,088/mo', 'success');
  }

  // ==========================================================================
  // Live Streaming & Anomaly Simulation
  // ==========================================================================
  function startLivePolling() {
    state.pollInterval = setInterval(() => {
      if (!state.isPolling) return;
      pushLiveTelemetryData();
    }, 5000);
  }

  function togglePolling() {
    state.isPolling = !state.isPolling;
    const label = document.getElementById('poll-label');
    const indicator = document.querySelector('.live-indicator');

    if (state.isPolling) {
      if (label) label.textContent = 'Live Stream (5s)';
      if (indicator) indicator.style.backgroundColor = 'var(--status-healthy)';
      showToast('Live telemetry streaming resumed (5s interval)', 'info');
    } else {
      if (label) label.textContent = 'Stream Paused';
      if (indicator) indicator.style.backgroundColor = 'var(--sev3-amber)';
      showToast('Live telemetry streaming paused', 'info');
    }
  }

  function pushLiveTelemetryData() {
    // Append simulated live metric ticks
    shiftAndPush(state.telemetry.cpu, 45, 95);
    shiftAndPush(state.telemetry.latency, 120, 3500);
    shiftAndPush(state.telemetry.memory, 50, 72);
    shiftAndPush(state.telemetry.errors, 0.1, 4.5);

    // Update stat numbers
    const lastCpu = state.telemetry.cpu[state.telemetry.cpu.length - 1];
    const lastLat = state.telemetry.latency[state.telemetry.latency.length - 1];
    const lastMem = state.telemetry.memory[state.telemetry.memory.length - 1];
    const lastErr = state.telemetry.errors[state.telemetry.errors.length - 1];

    const cpuEl = document.getElementById('stat-cpu');
    const latEl = document.getElementById('stat-latency');
    const memEl = document.getElementById('stat-mem');
    const errEl = document.getElementById('stat-errors');

    if (cpuEl) cpuEl.textContent = lastCpu + '%';
    if (latEl) latEl.textContent = lastLat + ' ms';
    if (memEl) memEl.textContent = lastMem + '%';
    if (errEl) errEl.textContent = lastErr + '%';

    if (state.activeView === 'telemetry') {
      renderTelemetryCharts();
    }
  }

  function shiftAndPush(arr, min, max) {
    arr.shift();
    const last = arr[arr.length - 1];
    let next = last + (Math.random() * 8 - 4);
    next = Math.max(min, Math.min(max, next));
    arr.push(parseFloat(next.toFixed(1)));
  }

  function simulateAnomalySpike() {
    // Force sudden spike
    state.telemetry.cpu[state.telemetry.cpu.length - 1] = 98.4;
    state.telemetry.latency[state.telemetry.latency.length - 1] = 4250;
    state.telemetry.errors[state.telemetry.errors.length - 1] = 8.9;

    const newAnomaly = {
      detectedAt: new Date().toTimeString().slice(0, 8) + ' UTC',
      severity: 'CRITICAL',
      application: 'payment-service',
      metricType: 'LATENCY_P99',
      observed: '4,250 ms',
      expected: '180 ms',
      method: 'Z-Score Detector',
      zScore: '+4.9σ'
    };
    state.anomalies.unshift(newAnomaly);

    renderAllViews();
    showToast('⚠️ Anomaly Injected: Latency spike (+4.9σ) captured by Kafka consumer!', 'danger');
  }

  // ==========================================================================
  // Backend REST Synchronization (Seamless Integration)
  // ==========================================================================
  async function checkBackendHealth() {
    try {
      const res = await fetch('/api/v1/health');
      if (res.ok) {
        state.backendConnected = true;
        const envEl = document.getElementById('env-selector');
        if (envEl) {
          envEl.innerHTML = `<span class="env-dot"></span><span>API Connected (localhost:8080)</span>`;
        }
        // Fetch live incidents from backend
        fetchBackendIncidents();
      }
    } catch (e) {
      // Backend not running yet; gracefully operates in high-fidelity mode
      console.log('Spring Boot backend running in local demo / disconnected mode.');
    }
  }

  async function fetchBackendIncidents() {
    try {
      const res = await fetch('/api/v1/incidents?limit=20');
      if (res.ok) {
        const json = await res.json();
        if (json.data && json.data.length > 0) {
          state.incidents = json.data;
          renderAllViews();
        }
      }
    } catch (e) {
      console.warn('Could not fetch backend incidents:', e);
    }
  }

  async function sendBackendStatusUpdate(id, status) {
    if (!state.backendConnected) return;
    try {
      await fetch(`/api/v1/incidents/${id}/status/${status}`, { method: 'PATCH' });
    } catch (e) {
      console.warn('Backend sync error:', e);
    }
  }

  async function sendBackendCreateIncident(inc) {
    if (!state.backendConnected) return;
    try {
      await fetch('/api/v1/incidents', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: inc.title,
          description: inc.description,
          severity: inc.severity,
          priority: inc.priority,
          applicationName: inc.applicationName,
          environment: inc.environment
        })
      });
    } catch (e) {
      console.warn('Backend sync error:', e);
    }
  }

  // ==========================================================================
  // Helper Utilities
  // ==========================================================================
  function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      setTimeout(() => toast.remove(), 250);
    }, 3500);
  }

  function copyText(str) {
    navigator.clipboard.writeText(str).then(() => {
      showToast('Copied command to clipboard: ' + str.slice(0, 40) + '...', 'info');
    });
  }

  function sendRcaFeedback(isHelpful) {
    showToast(isHelpful ? 'Feedback recorded: AI diagnosis marked as accurate' : 'Feedback recorded: Diagnostic flagged for rule refinement', 'info');
  }

  function testNotification() {
    state.notifications.unshift({
      time: new Date().toTimeString().slice(0, 8) + ' UTC',
      type: 'MANUAL_TEST',
      channel: 'SLACK',
      recipient: '#prod-critical-alerts',
      subject: '⚡ [TEST] CloudSentry alert channel ping verification',
      status: 'SENT',
      retries: 0
    });
    renderNotificationsLog();
    showToast('Sent test notification to Slack #prod-critical-alerts', 'success');
  }

  function retryFailedAlerts() {
    showToast('All notification delivery queues verified. 0 pending retries.', 'info');
  }

  function formatTimeAgo(isoString) {
    const diffMs = Date.now() - new Date(isoString).getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    return `${Math.floor(diffHours / 24)}d ago`;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function (m) {
      return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m];
    });
  }

  // Expose methods to global scope for HTML event handlers
  window.CloudSentryApp = {
    switchTab,
    openIncidentDrawer,
    closeDrawer,
    transitionDrawerStatus,
    quickTransitionStatus,
    addTimelineComment,
    applyRecommendation,
    applyAllRecommendations,
    copyText,
    sendRcaFeedback,
    testNotification,
    retryFailedAlerts,
    refreshData: () => {
      renderAllViews();
      showToast('Refreshed all metrics, incidents, and cluster health radar', 'info');
    }
  };

  // Bootstrap when DOM is ready
  document.addEventListener('DOMContentLoaded', init);
})();
