package com.towelove.system.service.mail;


import com.towelove.common.core.domain.PageResult;

import com.towelove.system.domain.mail.MailAccountDO;
import com.towelove.system.domain.mail.vo.account.MailAccountCreateReqVO;
import com.towelove.system.domain.mail.vo.account.MailAccountPageReqVO;
import com.towelove.system.domain.mail.vo.account.MailAccountUpdateReqVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 邮箱账号 Service 接口
 *
 * @author: 张锦标
 * @since 2022-03-21
 */
public interface MailAccountService {

    /**
     * 初始化邮箱账号的本地缓存
     */
    void initLocalCache();

    /**
     * 从缓存中获取邮箱账号
     *
     * @param id 编号
     * @return 邮箱账号
     */
    MailAccountDO getMailAccountFromCache(Long id);
    /**
     * 创建邮箱账号
     *
     * @param createReqVO 邮箱账号信息
     * @return 编号
     */
    Long createMailAccount(@Valid MailAccountCreateReqVO createReqVO);

    /**
     * 修改邮箱账号
     *
     * @param updateReqVO 邮箱账号信息
     */
    void updateMailAccount(@Valid MailAccountUpdateReqVO updateReqVO);

    /**
     * 删除邮箱账号
     *
     * @param id 编号
     */
    void deleteMailAccount(Long id);

    /**
     * 获取邮箱账号信息
     *
     * @param id 编号
     * @return 邮箱账号信息
     */
    MailAccountDO getMailAccount(Long id);

    /**
     * 获取邮箱账号分页信息
     *
     * @param pageReqVO 邮箱账号分页参数
     * @return 邮箱账号分页信息
     */
    PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO);

    /**
     * 获取邮箱数组信息
     *
     * @return 邮箱账号信息数组
     */
    List<MailAccountDO> getMailAccountList();

}
