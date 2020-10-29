import React from 'react'
import { connect } from 'react-redux'
import logo from '@/assets/logo.svg'
import {
    Link,
    useLocation
} from 'react-router-dom'
import { Layout, Menu, Avatar, Dropdown, Space } from 'antd'
import {
    MenuUnfoldOutlined,
    MenuFoldOutlined,
    NodeIndexOutlined,
    UserOutlined,
    DashboardOutlined,
    LogoutOutlined,
    BlockOutlined
} from '@ant-design/icons'

function BasicLayout(props) {
    let [state, setState] = React.useState({
        collapsed: false,
    })
    const toggle = () => {
        setState({
            collapsed: !state.collapsed,
        })
    }
    let { pathname } = useLocation()

    return (
        <Layout>
            <Layout.Sider trigger={null} collapsible
                collapsed={state.collapsed}
                style={{
                    height: '100vh',
                }}>
                <Link to="/">
                    <div className="d-flex flex-row">
                        <img className="flex-grow-1 ma-3" src={logo} style={{ height: '32px' }} alt="logo" />
                    </div>
                </Link>
                <Menu className="overflow-x-hidden overflow-y-auto" style={{
                    height: 'calc(100vh - 56px)',
                }} theme="dark" mode="inline" selectedKeys={[pathname]} defaultOpenKeys={['list']}>
                    <Menu.SubMenu
                        key="list"
                        title={
                            <span>
                                <DashboardOutlined />
                                <span>仪表盘</span>
                            </span>
                        }
                    >
                        <Menu.Item key={`/node`} icon={<NodeIndexOutlined />}>
                            <Link to={`/node`}>节点</Link>
                        </Menu.Item>
                        <Menu.Item key={`/blockchain`} icon={<BlockOutlined />}>
                            <Link to={`/blockchain`}>链</Link>
                        </Menu.Item>
                        <Menu.Item key={`/wallet`} icon={<BlockOutlined />}>
                            <Link to={`/wallet`}>钱包</Link>
                        </Menu.Item>
                        <Menu.Item key={`/miner`} icon={<BlockOutlined />}>
                            <Link to={`/miner`}>矿工</Link>
                        </Menu.Item>
                    </Menu.SubMenu>
                </Menu>
            </Layout.Sider>
            <Layout style={{ height: '100vh' }}>
                <Layout.Header className="white d-flex flex-row" style={{ padding: 0 }}>
                    {state.collapsed
                        ? <MenuUnfoldOutlined className='px-6 flex-grow-0 d-flex align-center' style={{ fontSize: '18px' }} onClick={toggle} />
                        : <MenuFoldOutlined className='px-6 flex-grow-0 d-flex align-center' style={{ fontSize: '18px' }} onClick={toggle} />}
                    <span className="flex-grow-1"></span>
                    <span className="flex-grow-0 mr-10">
                        <Dropdown overlay={(
                            <Menu>
                                <Menu.Item icon={<UserOutlined />}>
                                    <a target="_blank" rel="noopener noreferrer" href="http://www.baidu.com/">
                                        个人中心
                                    </a>
                                </Menu.Item>
                                <Menu.Divider />
                                <Menu.Item danger icon={<LogoutOutlined />}>登出</Menu.Item>
                            </Menu>
                        )}>
                            <Space style={{ cursor: 'pointer' }}>
                                <Avatar icon={<UserOutlined />} />
                                <span>{props.userInfo && props.userInfo.name}</span>
                            </Space>
                        </Dropdown>
                    </span>
                </Layout.Header>
                <Layout.Content>
                    {props.children}
                </Layout.Content>
                <Layout.Footer className="text-center pa-2 grey--text">
                    <span>©{new Date().getFullYear()} Zhou Ziqiang. All rights reserved. </span>
                </Layout.Footer>
            </Layout>
        </Layout>
    )
}
export default connect(
    state => ({
        userInfo: state.UserinfoReducer
    })
)(BasicLayout)
