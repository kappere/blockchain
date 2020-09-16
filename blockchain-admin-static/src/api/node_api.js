import request from '@/utils/request'
export default {
    nodeList() {
        return request.get('/node/list?stable=false', {})
    }
}