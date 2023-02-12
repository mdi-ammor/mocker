
import React, { useState, useEffect } from "react";
import axios from "axios";
import ReactJson from 'react-json-view';
import AlertDialog from "../alert-dialog/AlertDialog";
import Info from "../../svg/info.svg";

import './RigthSide.css'

function RigthSide(props) {

    const [content, setContent] = useState(null);
    const [endpoint, setEndpoint] = useState(null);
    const [path, setPath] = useState('');
    const [method, setMethod] = useState('');
    const [mode, setMode] = useState('');
    const [dialog, setDialog] = useState(false);

    useEffect(() => {
        if(props.data) {
            const ept = props.data.endpoint;
            setContent(props.data.response);
            setEndpoint(ept);
            ept.path ? setPath(ept.path) : setPath('');
            ept.method ? setMethod(ept.method) : setMethod('');
            ept.mode ? setMode(ept.mode) : setMode('');
        }
    }, [props]);

    const save = () => {
        const url = `http://localhost:9000/ihm/endpoints/${endpoint.path.replace(/\//g, '')}`;
        const data = {
            endpoint: {...endpoint, path:path, method: method, mode:mode},
            response: content
        }

        axios.post(url, data).then(response => {
            if(response.status === 200) {
                setDialog(true);
            }
        });
        
    }

    const closeDialog = () => {
        setDialog(false);
    }

    const showDialog = () => {
        if(dialog)
            return (
                <AlertDialog title="Success" 
                    message="The endpoint has been modified successfully" 
                    buttonValue="Close" 
                    closeDialog={closeDialog}
                />
            )
    }

    if(endpoint && content) {
        return (
            <div className="rightSide">
                <div className="rightSide-endpoint-detail">
                    <div>
                        <span>Path&nbsp;&nbsp;:&nbsp;&nbsp;</span> 
                        <input type="text" className="rightSide-zone" value={path} onChange={e => setPath(e.target.value)}/>
                    </div>
                    <div>
                        <span>Method&nbsp;&nbsp;:&nbsp;&nbsp;</span>
                        <select value={method} className="rightSide-zone" onChange={e => setMethod(e.target.value)}>
                        <option value=""></option>
                        <option value="GET">GET</option>
                        <option value="POST">POST</option>
                        <option value="PUT">PUT</option>
                        <option value="DELETE">DELETE</option>
                        </select>
                    </div>
                    <div>
                        <span>Mode&nbsp;&nbsp;:&nbsp;&nbsp;</span>
                        <select value={mode} className="rightSide-zone" onChange={e => setMode(e.target.value)}>
                        <option value=""></option>
                        <option value="SIMPLE">SIMPLE</option>
                        <option value="CUSTOM">CUSTOM</option>
                        </select>
                    </div>
                </div>
                <div>
                    <ReactJson
                        style={{ padding: '1em', textAlign: 'left' }}
                        name={false}
                        theme='paraiso'
                        displayDataTypes={false}
                        enableClipboard={false}
                        onEdit={(edit)=> setContent(edit.updated_src)}
                        onAdd={(add)=> setContent(add.updated_src)}
                        onDelete={(del)=> setContent(del.updated_src)}
                        src={content}
                    />
                    <button className="rigthSide-button rightSide-primary" onClick={save}>Save</button>
                </div>
                {showDialog()}
            </div>
        );
    }
        
    return (
        <div className="rightSide-empty">
            <div className="rightSide-empty-message">
                <img src={Info} alt="Info" className="rightSide-empty-img" />
                <br />
                <span className="rightSide-empty-text">Choose from the list the endpoint to modify</span>
            </div>
        </div>
    );
}
export default RigthSide;