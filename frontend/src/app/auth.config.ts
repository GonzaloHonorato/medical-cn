export const authConfig = {
  clientId: '66b136ef-6f18-4168-a848-15aff1919111',
  authority:
    'https://ghdevcompany.b2clogin.com/ghdevcompany.onmicrosoft.com/B2C_1_dn-gh',
  knownAuthorities: ['ghdevcompany.b2clogin.com'],
  redirectUri:
    window.location.hostname === 'localhost'
      ? 'http://localhost:4200'
      : window.location.origin,
  navigateToLoginRequestUrl: false
};
