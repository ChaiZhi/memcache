/**
 * $Id$
 * Copyright(C) 2010-2016 happyelements.com. All rights reserved.
 */
package com.binaration.cache;

import net.spy.memcached.CASMutation;

/**
 *
 * @author <a href="mailto:well.cheng@happyelements.com">well.cheng</a>
 * @version 1.0
 * @since 1.0
 */
public abstract class EasyCASMutation<T> implements CASMutation<T> {
	public abstract T getNewValue(T oldValue);
}
