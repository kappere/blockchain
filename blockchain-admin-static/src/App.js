import React from 'react'
import './App.css'
import 'antd/dist/antd.css'
import renderRouter from '@/router'
import zhCN from 'antd/es/locale/zh_CN'
import { ConfigProvider } from 'antd'
import moment from 'moment'
import 'moment/locale/zh-cn'
import { Provider } from 'react-redux'
import store from './store'

moment.locale('zh-cn')
React.store = store

export default function App() {
    return (
        <Provider store={store}>
            <ConfigProvider locale={zhCN}>
                <div className="App">
                    {renderRouter()}
                </div>
            </ConfigProvider>
        </Provider>
    )
}
