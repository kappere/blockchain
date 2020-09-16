import request from '@/utils/request'
export default {
    wallets() {
        return request.get('/transaction/wallets', {})
    },
    createTransaction(walletName, address, value) {
        return request.post(`/transaction/createTransaction?walletName=${walletName}&address=${address}&value=${value}`, {})
    }
}