import React from 'react'
import {
    Route
} from 'react-router-dom'
import RouterGuard from '@/components/RouterGuard'
const renderRoutesMap = (routes) => (
    routes.map((route, index) => {
        return (
            <Route key={index} exact={!(route.children && route.children.length > 0)} path={route.path} render={props => (
                <RouterGuard {...route} {...props} />
            )}
            />
        )
    })
)
export default renderRoutesMap
