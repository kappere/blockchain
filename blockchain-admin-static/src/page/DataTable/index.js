import React from 'react'
import { Table, Tag, Space, Card, Button, message } from 'antd'
import {
    PlusOutlined
} from '@ant-design/icons'
import { Link } from 'react-router-dom'
import DataAddModal from './DataAddModal'

export default function DataTable() {
    const columns = [
        {
            title: 'Name',
            dataIndex: 'name',
            key: 'name',
            render: text => <Link to={`/helloWorld`}>{text}</Link>,
        },
        {
            title: 'Age',
            dataIndex: 'age',
            key: 'age',
        },
        {
            title: 'Address',
            dataIndex: 'address',
            key: 'address',
        },
        {
            title: 'Tags',
            key: 'tags',
            dataIndex: 'tags',
            render: tags => (
                <>
                    {tags.map(tag => {
                        let color = tag.length > 5 ? 'geekblue' : 'green';
                        if (tag === 'loser') {
                            color = 'volcano';
                        }
                        return (
                            <Tag color={color} key={tag}>
                                {tag.toUpperCase()}
                            </Tag>
                        );
                    })}
                </>
            ),
        },
        {
            title: 'Action',
            key: 'action',
            render: (text, record) => (
                <Space size="middle">
                    <Link to={`/helloWorld`}>Invite {record.name}</Link>
                    <Link to={`/helloWorld`}>Delete</Link>
                </Space>
            ),
        },
    ];

    const data = [
        {
            key: '1',
            name: 'John Brown',
            age: 32,
            address: 'New York No. 1 Lake Park',
            tags: ['nice', 'developer'],
        },
        {
            key: '2',
            name: 'Jim Green',
            age: 42,
            address: 'London No. 1 Lake Park',
            tags: ['loser'],
        },
        {
            key: '3',
            name: 'Joe Black',
            age: 32,
            address: 'Sidney No. 1 Lake Park',
            tags: ['cool', 'teacher'],
        },
    ]
    const [pages, setPages] = React.useState({
        current: 5,
        pageSize: 15,
        total: 103
    })
    const [showAddModal, setShowAddModal] = React.useState(false)
    return (
        <div className="ma-4">
            <Card>
                <div className="mb-2 d-flex justify-space-between">
                    <span className="subtitle-1">数据表格</span>
                    <span>
                        <Button type="primary" icon={<PlusOutlined />} onClick={() => setShowAddModal(true)}>新建</Button>
                    </span>
                </div>
                <Table columns={columns} dataSource={data}
                    pagination={{
                        showTotal: total => `共 ${total} 条`,
                        current: pages.current,
                        total: pages.total,
                        onChange: (page, pageSize) => {
                            setPages({
                                current: page,
                                pageSize: pageSize,
                                total: pages.total
                            })
                        }
                    }}
                />
                <DataAddModal
                    visible={showAddModal}
                    handleOk={(fields) => {
                        setShowAddModal(false)
                        message.success(JSON.stringify(fields))
                    }}
                    handleCancel={() => setShowAddModal(false)}>
                </DataAddModal>
            </Card>
        </div>
    )
}