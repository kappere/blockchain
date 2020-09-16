import React from 'react'
import Loadable from 'react-loadable'
import renderRoutesMap from './render-router-map'
import { useHistory  } from 'react-router-dom'
import { Spin } from 'antd'


export default function RouterGuard(props) {
    let { component, children = [], path, onEnter } = props
    if (path.endsWith('/')) { path = path.substr(0, path.length - 1) }
    let history = useHistory()
    if (!React.router) {
        React.router = history
    }
    React.useEffect(() => {
        if (onEnter) {
            onEnter()
        }
    }, [onEnter])
    if (!component) {
        return ''
    } else if (component.name !== 'component') {
        const DefaultComponent = component
        return (
            <DefaultComponent {...props} >
                {renderRoutesMap(children.map(item => ({
                    ...item,
                    path: path + item.path
                })))}
            </DefaultComponent>
        )
    } else {
        const LoadableComponent = Loadable({
            loader: component,
            loading: () => (
                <Spin style={{ width: '100%', marginTop: '100px' }} size="large"></Spin>
            )
        })
        return (
            <LoadableComponent {...props} >
                {renderRoutesMap(children.map(item => ({
                    ...item,
                    path: path + item.path
                })))}
            </LoadableComponent>
        )
    }
}