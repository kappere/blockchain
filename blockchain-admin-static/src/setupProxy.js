const { createProxyMiddleware } = require('http-proxy-middleware')
module.exports = function (app) {
    app.use(
        '/_api/',
        createProxyMiddleware({
            target: 'http://192.168.0.129:8080/',
            changeOrigin: true,
            // pathRewrite: {
            //     '^/_api/': '/', // 重写路径
            // },
        })
    )
}