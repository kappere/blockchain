
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
            },
            {
                path: '/blockchain',
                component: () => import('@/page/Blockchain'),
            },
            {
                path: '/dataTable',
                component: () => import('@/page/DataTable'),
            },
            {
                path: '/wallet',
                component: () => import('@/page/Wallet'),
            },
        ]
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