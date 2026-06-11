package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.school.entity.*;
import com.school.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/album")
@CrossOrigin
public class ClassAlbumController {

    private final String uploadPath = "/app/uploads/";
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    ));
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    @Autowired private ClassAlbumService albumService;
    @Autowired private ClassAlbumImageService imageService;
    @Autowired private ClassAlbumCommentService commentService;
    @Autowired private ClassAlbumLikeService likeService;
    @Autowired private ClassesService classesService;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer isFeatured,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<ClassAlbum> wrapper = new QueryWrapper<>();
        if (classId != null) wrapper.eq("class_id", classId);
        if (isFeatured != null) wrapper.eq("is_featured", isFeatured);
        wrapper.orderByDesc("activity_date", "create_time");
        List<ClassAlbum> list = albumService.list(wrapper);
        result.put("success", true);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/my-class")
    public Map<String, Object> myClassAlbums(@RequestParam Integer classId) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<ClassAlbum> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", classId);
        wrapper.orderByDesc("activity_date", "create_time");
        List<ClassAlbum> list = albumService.list(wrapper);
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @GetMapping("/featured")
    public Map<String, Object> featuredAlbums(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<ClassAlbum> wrapper = new QueryWrapper<>();
        wrapper.eq("is_featured", 1);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT " + limit);
        List<ClassAlbum> list = albumService.list(wrapper);
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Integer id,
                                      @RequestParam(required = false) Integer userId,
                                      @RequestParam(required = false, defaultValue = "student") String userType) {
        Map<String, Object> result = new HashMap<>();
        ClassAlbum album = albumService.getById(id);
        if (album == null) {
            result.put("success", false);
            result.put("message", "相册不存在");
            return result;
        }

        if (album.getViewCount() == null) album.setViewCount(0);
        album.setViewCount(album.getViewCount() + 1);
        albumService.updateById(album);

        QueryWrapper<ClassAlbumImage> imgWrapper = new QueryWrapper<>();
        imgWrapper.eq("album_id", id);
        imgWrapper.orderByAsc("sort_order", "id");
        List<ClassAlbumImage> images = imageService.list(imgWrapper);

        QueryWrapper<ClassAlbumComment> cmtWrapper = new QueryWrapper<>();
        cmtWrapper.eq("album_id", id);
        cmtWrapper.orderByDesc("create_time");
        List<ClassAlbumComment> comments = commentService.list(cmtWrapper);

        boolean liked = false;
        if (userId != null) {
            QueryWrapper<ClassAlbumLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("album_id", id);
            likeWrapper.eq("user_id", userId);
            likeWrapper.eq("user_type", userType);
            liked = likeService.count(likeWrapper) > 0;
        }

        result.put("success", true);
        result.put("album", album);
        result.put("images", images);
        result.put("comments", comments);
        result.put("liked", liked);
        return result;
    }

    @PostMapping("/save")
    @Transactional
    public Map<String, Object> save(@RequestBody ClassAlbum album) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (album.getClassId() != null) {
                Classes cls = classesService.getById(album.getClassId());
                if (cls != null) album.setClassName(cls.getCname());
            }
            if (album.getId() == null) {
                if (album.getLikeCount() == null) album.setLikeCount(0);
                if (album.getViewCount() == null) album.setViewCount(0);
                if (album.getCommentCount() == null) album.setCommentCount(0);
                if (album.getIsFeatured() == null) album.setIsFeatured(0);
            }
            albumService.saveOrUpdate(album);
            result.put("success", true);
            result.put("data", album);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public Map<String, Object> delete(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<ClassAlbumImage> imgWrapper = new QueryWrapper<>();
            imgWrapper.eq("album_id", id);
            List<ClassAlbumImage> images = imageService.list(imgWrapper);
            for (ClassAlbumImage img : images) {
                if (img.getImagePath() != null) {
                    File f = new File(uploadPath + img.getImagePath().replace("/uploads/", ""));
                    if (f.exists()) f.delete();
                }
            }

            QueryWrapper<ClassAlbumComment> cmtWrapper = new QueryWrapper<>();
            cmtWrapper.eq("album_id", id);
            List<ClassAlbumComment> comments = commentService.list(cmtWrapper);
            for (ClassAlbumComment c : comments) {
                if (c.getImagePath() != null) {
                    File f = new File(uploadPath + c.getImagePath().replace("/uploads/", ""));
                    if (f.exists()) f.delete();
                }
            }

            ClassAlbum album = albumService.getById(id);
            if (album != null && album.getCoverImage() != null) {
                File f = new File(uploadPath + album.getCoverImage().replace("/uploads/", ""));
                if (f.exists() && !images.stream().anyMatch(i -> album.getCoverImage().equals(i.getImagePath()))) {
                    f.delete();
                }
            }

            albumService.removeById(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/upload-images")
    public Map<String, Object> uploadImages(
            @RequestParam("albumId") Integer albumId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) Integer uploaderId,
            @RequestParam(required = false) String uploaderName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploaded = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        if (files == null || files.length == 0) {
            result.put("success", false);
            result.put("message", "请选择要上传的图片");
            return result;
        }

        ClassAlbum album = albumService.getById(albumId);
        if (album == null) {
            result.put("success", false);
            result.put("message", "相册不存在");
            return result;
        }

        int maxSort = 0;
        QueryWrapper<ClassAlbumImage> sortWrapper = new QueryWrapper<>();
        sortWrapper.eq("album_id", albumId);
        sortWrapper.orderByDesc("sort_order");
        sortWrapper.last("LIMIT 1");
        ClassAlbumImage lastImg = imageService.getOne(sortWrapper);
        if (lastImg != null && lastImg.getSortOrder() != null) {
            maxSort = lastImg.getSortOrder();
        }

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map<String, Object> item = new HashMap<>();
            item.put("originalName", file.getOriginalFilename());
            try {
                if (file.isEmpty()) {
                    item.put("success", false);
                    item.put("message", "文件为空");
                    failed.add(item);
                    continue;
                }

                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    item.put("success", false);
                    item.put("message", "不支持的图片类型，仅支持 JPG/PNG/GIF/WEBP/BMP");
                    failed.add(item);
                    continue;
                }

                if (file.getSize() > MAX_IMAGE_SIZE) {
                    item.put("success", false);
                    item.put("message", "图片大小不能超过 10MB");
                    failed.add(item);
                    continue;
                }

                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;

                File dest = new File(uploadPath + newFilename);
                file.transferTo(dest);

                ClassAlbumImage img = new ClassAlbumImage();
                img.setAlbumId(albumId);
                img.setImagePath("/uploads/" + newFilename);
                img.setImageName(originalFilename);
                img.setImageSize(file.getSize());
                img.setSortOrder(maxSort + i + 1);
                img.setIsCover(0);
                img.setUploaderId(uploaderId);
                img.setUploaderName(uploaderName);
                img.setCreateTime(LocalDateTime.now());
                imageService.save(img);

                if (album.getCoverImage() == null || album.getCoverImage().isEmpty()) {
                    album.setCoverImage(img.getImagePath());
                    albumService.updateById(album);
                    img.setIsCover(1);
                    imageService.updateById(img);
                }

                item.put("success", true);
                item.put("id", img.getId());
                item.put("url", img.getImagePath());
                item.put("size", img.getImageSize());
                uploaded.add(item);
            } catch (IOException e) {
                item.put("success", false);
                item.put("message", "上传失败: " + e.getMessage());
                failed.add(item);
            }
        }

        result.put("success", true);
        result.put("uploaded", uploaded);
        result.put("failed", failed);
        result.put("total", files.length);
        result.put("successCount", uploaded.size());
        result.put("failCount", failed.size());
        return result;
    }

    @PostMapping("/image/set-cover")
    @Transactional
    public Map<String, Object> setCover(@RequestBody Map<String, Integer> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer albumId = params.get("albumId");
            Integer imageId = params.get("imageId");

            ClassAlbumImage img = imageService.getById(imageId);
            if (img == null || !img.getAlbumId().equals(albumId)) {
                result.put("success", false);
                result.put("message", "图片不存在");
                return result;
            }

            QueryWrapper<ClassAlbumImage> wrapper = new QueryWrapper<>();
            wrapper.eq("album_id", albumId);
            List<ClassAlbumImage> images = imageService.list(wrapper);
            for (ClassAlbumImage i : images) {
                i.setIsCover(0);
            }
            imageService.updateBatchById(images);

            img.setIsCover(1);
            imageService.updateById(img);

            ClassAlbum album = albumService.getById(albumId);
            if (album != null) {
                album.setCoverImage(img.getImagePath());
                albumService.updateById(album);
            }

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置封面失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/image/sort")
    public Map<String, Object> sortImages(@RequestBody List<Map<String, Integer>> sortList) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (Map<String, Integer> item : sortList) {
                Integer id = item.get("id");
                Integer order = item.get("sortOrder");
                ClassAlbumImage img = imageService.getById(id);
                if (img != null) {
                    img.setSortOrder(order);
                    imageService.updateById(img);
                }
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "排序失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/image/delete/{id}")
    @Transactional
    public Map<String, Object> deleteImage(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            ClassAlbumImage img = imageService.getById(id);
            if (img == null) {
                result.put("success", false);
                result.put("message", "图片不存在");
                return result;
            }

            if (img.getImagePath() != null) {
                File f = new File(uploadPath + img.getImagePath().replace("/uploads/", ""));
                if (f.exists()) f.delete();
            }

            Integer albumId = img.getAlbumId();
            boolean wasCover = img.getIsCover() != null && img.getIsCover() == 1;
            imageService.removeById(id);

            if (wasCover) {
                QueryWrapper<ClassAlbumImage> wrapper = new QueryWrapper<>();
                wrapper.eq("album_id", albumId);
                wrapper.orderByAsc("sort_order", "id");
                wrapper.last("LIMIT 1");
                ClassAlbumImage firstImg = imageService.getOne(wrapper);
                ClassAlbum album = albumService.getById(albumId);
                if (firstImg != null && album != null) {
                    firstImg.setIsCover(1);
                    imageService.updateById(firstImg);
                    album.setCoverImage(firstImg.getImagePath());
                    albumService.updateById(album);
                } else if (album != null) {
                    album.setCoverImage(null);
                    albumService.updateById(album);
                }
            }

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/like")
    @Transactional
    public Map<String, Object> toggleLike(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer albumId = (Integer) params.get("albumId");
            Integer userId = (Integer) params.get("userId");
            String userType = (String) params.getOrDefault("userType", "student");

            QueryWrapper<ClassAlbumLike> wrapper = new QueryWrapper<>();
            wrapper.eq("album_id", albumId);
            wrapper.eq("user_id", userId);
            wrapper.eq("user_type", userType);
            ClassAlbumLike existing = likeService.getOne(wrapper);

            ClassAlbum album = albumService.getById(albumId);
            if (album == null) {
                result.put("success", false);
                result.put("message", "相册不存在");
                return result;
            }

            if (existing != null) {
                likeService.removeById(existing.getId());
                if (album.getLikeCount() == null) album.setLikeCount(0);
                album.setLikeCount(Math.max(0, album.getLikeCount() - 1));
                result.put("liked", false);
            } else {
                ClassAlbumLike like = new ClassAlbumLike();
                like.setAlbumId(albumId);
                like.setUserId(userId);
                like.setUserType(userType);
                like.setCreateTime(LocalDateTime.now());
                likeService.save(like);
                if (album.getLikeCount() == null) album.setLikeCount(0);
                album.setLikeCount(album.getLikeCount() + 1);
                result.put("liked", true);
            }
            albumService.updateById(album);

            result.put("success", true);
            result.put("likeCount", album.getLikeCount());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/comment/add")
    @Transactional
    public Map<String, Object> addComment(@RequestBody ClassAlbumComment comment) {
        Map<String, Object> result = new HashMap<>();
        try {
            comment.setCreateTime(LocalDateTime.now());
            commentService.save(comment);

            ClassAlbum album = albumService.getById(comment.getAlbumId());
            if (album != null) {
                if (album.getCommentCount() == null) album.setCommentCount(0);
                album.setCommentCount(album.getCommentCount() + 1);
                albumService.updateById(album);
            }

            result.put("success", true);
            result.put("data", comment);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评论失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/comment/delete/{id}")
    @Transactional
    public Map<String, Object> deleteComment(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            ClassAlbumComment comment = commentService.getById(id);
            if (comment == null) {
                result.put("success", false);
                result.put("message", "评论不存在");
                return result;
            }
            if (comment.getImagePath() != null) {
                File f = new File(uploadPath + comment.getImagePath().replace("/uploads/", ""));
                if (f.exists()) f.delete();
            }
            Integer albumId = comment.getAlbumId();
            commentService.removeById(id);

            ClassAlbum album = albumService.getById(albumId);
            if (album != null) {
                if (album.getCommentCount() == null) album.setCommentCount(0);
                album.setCommentCount(Math.max(0, album.getCommentCount() - 1));
                albumService.updateById(album);
            }

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/toggle-featured")
    public Map<String, Object> toggleFeatured(@RequestBody Map<String, Integer> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = params.get("id");
            ClassAlbum album = albumService.getById(id);
            if (album == null) {
                result.put("success", false);
                result.put("message", "相册不存在");
                return result;
            }
            album.setIsFeatured(album.getIsFeatured() == null || album.getIsFeatured() == 0 ? 1 : 0);
            albumService.updateById(album);
            result.put("success", true);
            result.put("isFeatured", album.getIsFeatured());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }
}
