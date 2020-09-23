import React from 'react'
import {
    BrowserRouter as Router,
    Switch,
} from 'react-router-dom'
import renderRoutesMap from '@/components/RouterGuard/render-router-map'
import BasicLayout from '@/layout/BasicLayout'
import {UserActions} from '@/store/action'

const routerConfig = [
    {
        path: '/',
        component: BasicLayout,
        onEnter: () => {
            React.store.dispatch({ type: UserActions.SET_USERINFO, text: {name: 'Admin'}})
        },
        children: [
            {
                path: '/',
                onEnter: () => {
                    React.router.push('/node')
                }
            },
            {
                path: '/node',
                component: () => import('@/page/Node'),
                meta: {
                    title: '节点 - 区块链'
                }
            },
            {
                path: '/blockchain',
                component: () => import('@/page/Blockchain'),
            },
            {
                path: '/wallet',
                component: () => import('@/page/Wallet'),
                meta: {
                    title: '钱包 - 区块链'
                }
            },
        ],
        meta: {
            title: '区块链'
        },
    }
]

const renderRouter = ({ routes, extraProps = {}, switchProps = {} }) => (
    <Router>
        <Switch {...switchProps}>
            {renderRoutesMap(routes)}
        </Switch>
    </Router>
)

export default () => (
    renderRouter({
        routes: routerConfig
    })
)