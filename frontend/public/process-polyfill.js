(function injectProcessEnvPolyfill() {
	if (typeof window === 'undefined' || typeof window.process !== 'undefined') {
		return;
	}
	const fallbackEnv = { NODE_ENV: 'development' };
	function applyEnv(env) {
		window.process = { env: { ...fallbackEnv, ...env } };
	}
	fetch(new URL('Untitled-1.json', window.location.href), { cache: 'no-cache' })
		.then((response) => (response.ok ? response.json() : {}))
		.then(applyEnv)
		.catch(() => applyEnv({}));
})();