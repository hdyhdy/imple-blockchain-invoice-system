// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * 发票存证智能在合约
 * 用于区块链上存储发票的数字指纹，提供不可篡改的存证证明
 */
contract InvoiceRegistry {

    // 发票存证结构体
    struct InvoiceProof {
        string invoiceCode;      // 发票代码
        string invoiceNumber;     // 发票号码
        uint256 amount;           // 金额（分，避免小数）
        bytes32 dataHash;         // 发票数据的SHA256哈希
        uint256 timestamp;        // 存证时间戳
        address issuer;           // 开票方地址
        bool exists;              // 是否存在
    }

    // 存储发票存证映射
    // key: invoiceCode + invoiceNumber 的哈希
    mapping(bytes32 => InvoiceProof) public proofs;

    // 存储所有发票代码+号码的列表（用于遍历）
    bytes32[] public proofKeys;

    // 发票发行事件
    event InvoiceIssued(
        string indexed invoiceCode,
        string indexed invoiceNumber,
        uint256 amount,
        bytes32 dataHash,
        uint256 timestamp,
        address indexed issuer
    );

    // 发行发票
    function issueInvoice(
        string memory _code,
        string memory _num,
        uint256 _amount,
        bytes32 _hash
    ) public returns (bytes32) {
        // 生成唯一的key
        bytes32 key = keccak256(abi.encodePacked(_code, _num));

        // 检查是否已存在
        require(!proofs[key].exists, "Invoice already exists");

        // 创建存证
        proofs[key] = InvoiceProof({
            invoiceCode: _code,
            invoiceNumber: _num,
            amount: _amount,
            dataHash: _hash,
            timestamp: block.timestamp,
            issuer: msg.sender,
            exists: true
        });

        // 添加到keys数组
        proofKeys.push(key);

        // 触发事件
        emit InvoiceIssued(_code, _num, _amount, _hash, block.timestamp, msg.sender);

        return key;
    }

    // 验证发票是否存在
    function verifyInvoice(
        string memory _code,
        string memory _num,
        uint256 _amount
    ) public view returns (bool, bytes32, uint256) {
        bytes32 key = keccak256(abi.encodePacked(_code, _num));
        InvoiceProof memory proof = proofs[key];

        if (!proof.exists) {
            return (false, 0, 0);
        }

        if (proof.amount != _amount) {
            return (false, proof.dataHash, proof.timestamp);
        }

        return (true, proof.dataHash, proof.timestamp);
    }

    // 获取发票存证详情
    function getInvoiceProof(string memory _code, string memory _num)
        public
        view
        returns (
            bool exists,
            uint256 amount,
            bytes32 dataHash,
            uint256 timestamp,
            address issuer
        )
    {
        bytes32 key = keccak256(abi.encodePacked(_code, _num));
        InvoiceProof memory proof = proofs[key];

        return (
            proof.exists,
            proof.amount,
            proof.dataHash,
            proof.timestamp,
            proof.issuer
        );
    }

    // 获取已存证发票数量
    function getProofCount() public view returns (uint256) {
        return proofKeys.length;
    }
}
