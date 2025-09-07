package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.Transfer;
import io.renren.modules.app.mapper.TransferMapper;
import io.renren.modules.app.service.TransferService;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl extends ServiceImpl<TransferMapper, Transfer> implements TransferService {
}
