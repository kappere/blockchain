import { message } from 'antd'
import Axios from 'axios'
import qs from 'qs'

const REQ_PREFIX = '/_api'

const addTimestampToUrl = url => {
    if (url.indexOf('?') >= 0) {
        return url + '&_t=' + new Date().getTime()
    } else {
        return url + '?_t=' + new Date().getTime()
    }
}

const get = (url, params) => {
    url = addTimestampToUrl(url)
    return doPromise(Axios.get(`${REQ_PREFIX}${url}`, {
        params,
        paramsSerializer: params => {
            return qs.stringify(params)
        }
    }))
}

const post = (url, data) => {
    url = addTimestampToUrl(url)
    return doPromise(Axios.post(`${REQ_PREFIX}${url}`, data))
}

const doPromise = (promise) => {
    return promise.then(response => {
        if (response.data.success) {
            if (response.headers.redirect) {
                window.location.href = response.headers.redirect
                return Promise.reject(response.data)
            } else {
                return Promise.resolve(response.data.data)
            }
        } else {
            message.error(response.data.message)
            return Promise.reject(response.data)
        }
    }).catch(noAuthInterceptor)
}

const noAuthInterceptor = data => {
    if (data.code === -999) {
        window.location.href = '/login'
    } else if (data.code === -989) {
        message.error('用户无访问权限')
    }
    return Promise.reject(data)
}

export default {
    REQ_PREFIX,
    get,
    post
}