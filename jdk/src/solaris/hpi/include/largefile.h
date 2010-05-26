/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#ifndef _JAVASOFT_LARGEFILE_SUPPORT_H_
#define _JAVASOFT_LARGEFILE_SUPPORT_H_

#ifdef __solaris__
#include "largefile_solaris.h"
#endif

#ifdef __linux__
#include "largefile_linux.h"
#endif

/*
 * Prototypes for wrappers that we define.  These wrapper functions
 * are low-level I/O routines that will use 64 bit versions if
 * available, else revert to the 32 bit ones.
 */
extern off64_t lseek64_w(int fd, off64_t offset, int whence);
extern int fstat64_w(int fd, struct stat *buf);
extern int ftruncate64_w(int fd, off64_t length);
extern int open64_w(const char *path, int oflag, int mode);

/* This is defined in system_md.c */
extern int sysFfileMode(int fd, int* mode);

#endif /* _JAVASOFT_LARGEFILE_SUPPORT_H_ */
