package net.alienminds.ethnogram.data.repository

import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.common.PagingData
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor
import net.alienminds.ethnogram.data.model.user.User
import net.alienminds.ethnogram.data.model.user.UserFilter

interface UserRepository2 {

    fun getPublicNewUsers(paging: PagingInput): QueryRequestExecutor<PagingData<User>>

    fun getPublicTopUsers(paging: PagingInput): QueryRequestExecutor<PagingData<User>>

    fun getPublicActiveUsers(paging: PagingInput): QueryRequestExecutor<PagingData<User>>

    fun filterPublicUsers(
        paging: PagingInput,
        filter: UserFilter
    ): QueryRequestExecutor<PagingData<User>>

    fun getMe(): QueryRequestExecutor<User>

    fun getUser(id: ID): QueryRequestExecutor<User>

}