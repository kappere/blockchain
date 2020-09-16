import request from '@/utils/request'
export default {
    chainDetail() {
        return request.get('/chain/detail', {})
    }
}