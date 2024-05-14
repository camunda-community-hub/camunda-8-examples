/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  output: 'export',
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `http://localhost:8080/api/:path*`,
      },
    ]
  },
}


module.exports = nextConfig
