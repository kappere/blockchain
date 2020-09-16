import React from 'react'
import { Modal, Form, Input } from 'antd'

export default (props) => {
    const [form] = Form.useForm()
    React.useEffect(() => {
        const init = () => {
            form.resetFields()
        }
        props.visible && init()
    }, [props, form])
    return (
        <Modal
            title="新建"
            visible={props.visible}
            onOk={async () => {
                let fields = await form.validateFields()
                props.handleOk(fields)
            }}
            onCancel={props.handleCancel}
        >
            <Form form={form}>
                <Form.Item label="姓名" name="username" rules={[{ required: true, message: 'Please input your username!' }]}>
                    <Input></Input>
                </Form.Item>
                <Form.Item label="密码" name="password" rules={[{ required: true, message: 'Please input your password!' }]}>
                    <Input type="password"></Input>
                </Form.Item>
            </Form>
        </Modal>
    )
}